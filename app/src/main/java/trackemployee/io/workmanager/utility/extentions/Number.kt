package trackemployee.io.workmanager.utility.extentions

fun Number.roundOff(value: Int = 2) = String.format("%.${value}f".format(this))