package in.zeta.qa.email;

import org.testng.annotations.Test;

public class TestClass1 {

    @Test
    void  class1Test1() {
        System.out.println("class1Test1 Thread : " + Thread.currentThread().getName());
    }

    @Test
    void  class1Test2() {
        System.out.println("class1Test2 Thread : " + Thread.currentThread().getName());
    }

    @Test
    void  class1Test3() {
        System.out.println("class1Test3 Thread : " + Thread.currentThread().getName());
    }

}
