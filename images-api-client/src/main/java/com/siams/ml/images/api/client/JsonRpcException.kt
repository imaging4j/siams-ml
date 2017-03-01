package com.siams.ml.images.api.client

import java.io.IOException

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
class JsonRpcException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
