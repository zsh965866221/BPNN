package bpnn.function;

/**
 * Created by zsh_o on 2016/10/1.
 * 激活函数接口
 */
public interface ActivateFunction {
   double calculate(double x);
   double derivativeCalculate(double x);
}
