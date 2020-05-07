package ru.mirea

import javafx.stage.Stage
import org.opencv.core.Core
import ru.mirea.gui.MainWindow
import tornadofx.App
import tornadofx.beforeShutdown
import tornadofx.launch


class Main : App(MainWindow::class) {

    override fun start(stage: Stage) {
        super.start(stage)
        beforeShutdown {
            if (MainWindow.leftGrabber != null && MainWindow.leftGrabber!!.isThreadRun) {
                MainWindow.leftGrabber!!.initAndRunOrStop()
                print("here")
            }
            if (MainWindow.rightGrabber != null && MainWindow.rightGrabber!!.isThreadRun) {
                MainWindow.rightGrabber!!.initAndRunOrStop()
            }
            Thread.sleep(3000)
        }
    }
}

fun main() {
    val opencvpath = System.getProperty("user.dir") + "\\libraries\\"
    System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll")
    launch<Main>()
}
