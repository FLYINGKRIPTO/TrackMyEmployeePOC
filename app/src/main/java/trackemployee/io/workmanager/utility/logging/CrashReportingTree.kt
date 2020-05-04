package trackemployee.io.workmanager.utility.logging

import androidx.annotation.Nullable
import android.util.Log
import android.util.Log.INFO
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CrashReportingTree : Timber.Tree() {

    fun isLoggable(priority: Int, @Nullable tag: String): Boolean {
        return priority >= INFO
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || t is SocketTimeoutException || t is UnknownHostException)
            return

        FakeCrashLibrary.log(priority, tag ?: "", message)

        if (t != null) {
            if (priority == Log.ERROR) {
                FakeCrashLibrary.logError(t);
            } else if (priority == Log.WARN) {
                FakeCrashLibrary.logWarning(t);
            }
        }
    }

}