/**
 * Executes string as shell command and returns result as string.
 */
fun String.execute(): String = Runtime.getRuntime().exec(this)
        .inputStream.bufferedReader().use { it.readText() }
