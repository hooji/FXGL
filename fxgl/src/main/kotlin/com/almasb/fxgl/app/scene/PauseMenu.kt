/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.app.scene

import com.almasb.fxgl.animation.Animation
import com.almasb.fxgl.animation.Interpolators
import com.almasb.fxgl.core.util.EmptyRunnable
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.input.UserAction
import com.almasb.fxgl.scene.SubScene
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration

/**
 * Pause (in-game) menu is used instead of full Game menu when settings.isMenuEnabled is false.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
abstract class PauseMenu : SubScene() {

    private var canSwitchGameMenu = true

    init {
        input.addAction(object : UserAction("Resume") {
            override fun onActionBegin() {
                requestHide()
            }

            override fun onActionEnd() {
                unlockSwitch()
            }
        }, FXGL.getSettings().menuKey)
    }

    internal fun requestShow(onShow: () -> Unit) {
        if (canSwitchGameMenu) {
            canSwitchGameMenu = false
            onShow()
        }
    }

    protected fun requestHide() {
        if (canSwitchGameMenu) {
            canSwitchGameMenu = false
            onHide()
            unlockSwitch()
        }
    }

    internal fun unlockSwitch() {
        canSwitchGameMenu = true
    }

    protected open fun onHide() {
        FXGL.getSceneService().popSubScene()
    }
}

class FXGLPauseMenu : PauseMenu() {

    private val masker = Rectangle(FXGL.getAppWidth().toDouble(), FXGL.getAppHeight().toDouble(), Color.color(0.0, 0.0, 0.0, 0.25))
    private val content: Pane

    private val animation: Animation<*>

    init {
        content = createContentPane()
        content.children.add(createContent())

        content.translateX = FXGL.getAppWidth() / 2.0 - 125
        content.translateY = FXGL.getAppHeight() / 2.0 - 200

        contentRoot.children.addAll(masker, content)

        animation = FXGL.animationBuilder()
                .duration(Duration.seconds(0.5))
                .interpolator(Interpolators.BACK.EASE_OUT())
                .translate(content)
                .from(Point2D(FXGL.getAppWidth() / 2.0 - 125, -400.0))
                .to(Point2D(FXGL.getAppWidth() / 2.0 - 125, FXGL.getAppHeight() / 2.0 - 200))
                .build()
    }

    override fun onCreate() {
        animation.onFinished = EmptyRunnable
        animation.start()
    }

    override fun onUpdate(tpf: Double) {
        animation.onUpdate(tpf)
    }

    private fun createContentPane(): StackPane {
        return StackPane(FXGL.texture("pause_menu_bg.png"))
    }

    private fun createContent(): Parent {
        val btnResume = FXGL.getUIFactory().newButton(FXGL.localizedStringProperty("menu.resume"))
        btnResume.setOnAction {
            requestHide()
        }

        val btnExit = FXGL.getUIFactory().newButton(FXGL.localizedStringProperty("menu.exit"))
        btnExit.setOnAction {
            FXGL.getGameController().exit()
        }

        val vbox = VBox(15.0, btnResume, btnExit)
        vbox.alignment = Pos.CENTER
        vbox.setPrefSize(250.0, 400.0)

        return vbox
    }

    override fun onHide() {
        if (animation.isAnimating)
            return

        animation.onFinished = Runnable {
            FXGL.getSceneService().popSubScene()
        }
        animation.startReverse()
    }
}