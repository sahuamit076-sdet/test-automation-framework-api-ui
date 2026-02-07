package in.zeta.qa.email;


import org.testng.annotations.Test;

public class TestClass2 {
    @Test
    void  class2Test1() {
        System.out.println("class2Test1 Thread : " + Thread.currentThread().getName());
    }

    @Test
    void  class2Test2() {
        System.out.println("class2Test2 Thread : " + Thread.currentThread().getName());
    }

    @Test
    void  class2Test3() {
        System.out.println("class2Test3 Thread : " + Thread.currentThread().getName());
    }
}
