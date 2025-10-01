package top.yinaicheng.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 安全验签
 * @author yinaicheng
 */
@Target(ElementType.METHOD) /*注解的作用目标为方法*/
@Retention(RetentionPolicy.RUNTIME) /*注解会在class字节码文件中存在，在运行时可以通过反射获取到*/
public @interface SecurityVerificySignAnnotation
{

}
