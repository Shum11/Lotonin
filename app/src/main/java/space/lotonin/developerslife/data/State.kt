package space.lotonin.developerslife.data

enum class State {
    INIT,
    LOADING,
    PROBLEM_INTERNET,
    PROBLEM_NO_CONTENT,
    PROBLEM_IMAGE_DOWNLOAD,
    PROBLEM_SERVER_ERROR,
    OK
}