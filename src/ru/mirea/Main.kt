package ru.mirea

import javafx.stage.Stage
import org.opencv.core.Core
import ru.mirea.core.CamerasProcessor
import ru.mirea.gui.MainWindow
import tornadofx.App
import tornadofx.beforeShutdown
import tornadofx.launch


class Main : App(MainWindow::class) {

    override fun start(stage: Stage) {
        super.start(stage)
        beforeShutdown {
            if (CamerasProcessor.firstCamera != null && CamerasProcessor.firstCamera!!.isThreadRun) {
                CamerasProcessor.firstCamera!!.initAndRunOrStop()
            }
            if (CamerasProcessor.secondCamera != null && CamerasProcessor.secondCamera!!.isThreadRun) {
                CamerasProcessor.secondCamera!!.initAndRunOrStop()
            }
            if (CamerasProcessor.objectPosition != null && CamerasProcessor.objectPosition!!.calculating) {
                CamerasProcessor.objectPosition!!.stopCalculating()
            }
        }
    }
}

fun main() {
    val opencvpath = System.getProperty("user.dir") + "\\libraries\\"
    System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll")
    launch<Main>()
}
