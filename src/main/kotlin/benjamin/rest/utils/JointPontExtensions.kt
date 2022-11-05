package benjamin.rest.utils

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.MethodSignature

fun <T : Annotation> JoinPoint.getAnnotation(annotationClass: Class<T>): T =
    methodSignature.method.getAnnotation(annotationClass)

val JoinPoint.methodSignature: MethodSignature
    get() = signature as MethodSignature

fun JoinPoint.getArgument(paramName: String): Any {
    val paramIdx = methodSignature.parameterNames.toList().indexOf(paramName)
    return if (paramIdx >= 0) {
        args[paramIdx]
    } else {
        throw IllegalStateException("No such param $paramName")
    }
}
