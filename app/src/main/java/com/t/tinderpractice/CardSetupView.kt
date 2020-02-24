/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.t.tinderpractice

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.card_setup_layout.view.*
import kotlin.math.*

class CardSetupView : FrameLayout {
    private var mTouchSlop: Int = 0
    private var yCoOrdinate: Float = 0f
    private var xCoOrdinate: Float = 0f
    private var startTime: Long = 0L
    private var cardLeftTopX: Float = 0f
    private var cardLeftTopY: Float = 0f
    private var makeCardFree: Boolean = false
    private var centerCardTouchListener: OnTouchListener? = null

    constructor(context: Context) : super(context) {
        CardSetupView(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initialize()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    fun initialize() {
        //inflate view
        LayoutInflater.from(context).inflate(R.layout.card_setup_layout, this)
        CardSetupViewUtils.initialize(context)
        setupTouchListeners()
    }

    /**
     * Sets up listeners.
     * */
    private fun setupTouchListeners() {
        if (mTouchSlop == 0) {
            val vc = ViewConfiguration.get(context)
            mTouchSlop = vc.scaledTouchSlop
        }

        centerCardTouchListener = OnTouchListener { v, event ->
            handleTouchEvent(v, event)
            //todo: Check if a meaningful touch is intercepted, give chance to child view for touches
            true
        }

        centerCard.setOnTouchListener(centerCardTouchListener)
    }

    /**
     * Handle touch event on the center card.
     * */
    private fun handleTouchEvent(v: View, event: MotionEvent) {
        val endTime: Long
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                makeCardFree = false
                if (centerCard.left.toFloat() != 0f) {
                    cardLeftTopX = centerCard.left.toFloat()
                }
                cardLeftTopY = centerCard.top.toFloat()
                startTime = System.currentTimeMillis()
                xCoOrdinate = centerCard.x - event.rawX
                yCoOrdinate = centerCard.y - event.rawY
            }

            MotionEvent.ACTION_UP -> {
                endTime = System.currentTimeMillis()
                makeCardFree = false
                val movedCardCenterX: Float = centerCard.x - cardLeftTopX
                val movedCardCenterY: Float = centerCard.y - cardLeftTopY
                if (abs(movedCardCenterX) > 2 || abs(movedCardCenterY) > 2 && endTime > startTime + 500) {
                    dismissCardOrBringToOriginalPosition(startTime, endTime, movedCardCenterX, movedCardCenterY)
                } else if (endTime - startTime < 150) {
                    /*if (v.id == R.id.tagGroup || v.id == R.id.tags_list || v.id == R.id.tag_group_sv) {
                        if (!onChipGroupTap(event.x, event.y)) {
                            onClick(v)
                        }
                    } else if (swipeRightCardLayout == null && topicsForFirstJamLayout == null) {
                        onClick(v)
                    }*/
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val deltax: Float = cardLeftTopX - (event.rawX + xCoOrdinate)
                if (isSwipable(deltax) && abs(deltax) > mTouchSlop) {

                    val deltay: Float = cardLeftTopY - (event.rawY + yCoOrdinate)
                    val verticalDragSensitivity = 1.3f

                    if ((abs(deltax) > 0 || abs(deltay) > 0)
                        && abs(deltax) >= abs(deltay / verticalDragSensitivity) || makeCardFree
                    ) {

                        if (deltax > 2) {
                            makeCardFree = true
                        }

                        val radiusOfMovement =
                            sqrt(deltax.toDouble().pow(2.0) + deltay.toDouble().pow(2.0))
                        centerCard.animate()
                            .x(event.rawX + xCoOrdinate)
                            .y(event.rawY + yCoOrdinate)
                            .setDuration(0).start()

                        setAlphaForCard(abs(deltax).toDouble())
                        val rotationSensitivity = 0.3f
                        centerCard.rotation =
                            (radiusOfMovement / centerCard.height * sign(-deltax) * rotationSensitivity * getRotationForDisplacementOf(
                                deltay,
                                deltax
                            )).toFloat()
                    }
                }
            }
        }
    }

    /**
     * Set Alpha of View.
     * @param radiusOfMovement, alpha of views depends upon the distance
     * user has taken card away from its original position.
     * */
    private fun setAlphaForCard(radiusOfMovement: Double) {
        if (radiusOfMovement > 250) {
            val alphaSensitivity = 1.2f
            centerCard.alpha =
                (1f - abs(radiusOfMovement / centerCard.height * alphaSensitivity)).toFloat() + 0.4f
        } else {
            centerCard.alpha = 1f
        }
    }

    /**
     * Check if card can be dismissed or it can be brought back to its original position.
     * */
    private fun dismissCardOrBringToOriginalPosition(
        startTime: Long,
        endTime: Long,
        movedCardCenterX: Float,
        movedCardCenterY: Float
    ) {

        val velocity = abs(abs(movedCardCenterX) / (endTime - startTime)).toDouble()
        if (velocity > 1.3f || abs(movedCardCenterX) > 250) {
            //Card was swiped with a greater velocity or was dragged away too much, so we can dismiss the card.
            dismissCard(centerCard.rotation, movedCardCenterX, movedCardCenterY)
        } else {
            if (centerCard.left.toFloat() != 0f) {
                cardLeftTopX = centerCard.left.toFloat()
            }
            cardLeftTopY = centerCard.top.toFloat()

            //animate back to its original position
            centerCard
                .animate()
                .x(cardLeftTopX)
                .y(cardLeftTopY)
                .rotation(0f)
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    /**
     * Decides whether card is swipable or not.
     * */
    private fun isSwipable(movedCardCenterX: Float): Boolean {
        return true
    }

    /**
     * Dismiss the card by animating it away from the screen.
     * */
    private fun dismissCard(
        rotation: Float,
        movedCardCenterX: Float,
        movedCardCenterY: Float) {

        val runnable = Runnable {
            bringNewCard(movedCardCenterX > 0)
        }
        val distance =
            ((CardSetupViewUtils.getScreenHeight() * 1.5 - abs(movedCardCenterY)) / 2f).toFloat()
        val finalX =
            (centerCard.x + sign(rotation) * sign(movedCardCenterX) * 2 * distance * sin(PI / 180 * rotation * 3)).toFloat()
        val finalY = (centerCard.y + distance * cos(PI / 180 * rotation * 4)).toFloat()

        centerCard
            .animate()
            .x(finalX)
            .y(finalY)
            .alphaBy(-0.6f).setDuration(abs(distance / 3).toLong())
            .withEndAction(runnable).start()
    }

    /**
     * Brings new card to view with animation.
     * */
    private fun bringNewCard(bringNewCardFromLeft: Boolean) {
        centerCard.visibility = View.INVISIBLE
        centerCard.alpha = 1.0f
        centerCard.animate().alpha(1.0f)
        centerCard.x = cardLeftTopX
        centerCard.y = cardLeftTopY
        centerCard.rotation = 0f
        //show shimmer and start shimmer

        if (bringNewCardFromLeft) {
            val slideInFromLeftAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_slide_in_from_left)

            animateCenterCard(slideInFromLeftAnimation)

            //We need to hide right edge card to make it feel like, that card is moved
            // to center and a new card is coming on the right.
            leftEdgePeekingCard.visibility = View.INVISIBLE
            centerCard.postDelayed({ translateInLeftEdgeCard() }, 200)
        } else {
            //bring from right
            val slideInFromRightAnimation =
                AnimationUtils.loadAnimation(context, R.anim.anim_slide_in_from_right)
            animateCenterCard(slideInFromRightAnimation)
            //We need to hide right edge card to make it feel like, that card is moved
            // to center and a new card is coming on the right.
            rightEdgePeekingCard.visibility = View.INVISIBLE
            centerCard.postDelayed({ translateInRightEdgeCard()}, 200)
        }
    }

    /**
     * Animate right edge card.
     * */
    private fun translateInRightEdgeCard() {
        rightEdgePeekingCard.visibility = View.VISIBLE
        val slideInFromRightAnimationn = AnimationUtils.loadAnimation(context, R.anim.anim_slide_in_from_right)
        rightEdgePeekingCard.startAnimation(slideInFromRightAnimationn)
    }

    /**
     * Animate left edge card.
     * */
    private fun translateInLeftEdgeCard() {
        leftEdgePeekingCard.visibility = View.VISIBLE
        val slideInFromLeftAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_slide_in_from_left)
        leftEdgePeekingCard.startAnimation(slideInFromLeftAnimation)
    }

    /**
     * show transition animation when user goes to another jam or finish the current one
     */
    private fun animateCenterCard(animation: Animation) {
        centerCard.visibility = View.VISIBLE
        centerCard.startAnimation(animation)
    }

    /**
     * Calculate rotation angle for card given the delta in x and y direction.
     * */
    private fun getRotationForDisplacementOf(deltaX: Float, deltaY: Float): Double {
        //atan2() is an inbuilt method in Java which is used to return the theta component
        // from polar coordinate. The atan2() method returns a numeric value between –\pi and \pi
        // representing the angle \theta of a (x, y) point and positive x-axis.
        // It is the counterclockwise angle, measured in radian, between the positive
        // X-axis, and the point (x, y).
        val radians = atan2(abs(deltaY).toDouble(), abs(deltaX).toDouble())
        val degree = Math.toDegrees(radians)
        return when {
            degree > 90 -> {
                degree - 90
            }
            degree < -90 -> {
                degree + 90
            }
            else -> {
                degree
            }
        }
    }

}