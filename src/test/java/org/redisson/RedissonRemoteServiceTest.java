package org.redisson;

import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

public class RedissonRemoteServiceTest extends BaseTest {

    public interface RemoteInterface {
        
        void voidMethod(String name, Long param);
        
        Long resultMethod(Long value);
        
        void errorMethod() throws IOException;
        
        void errorMethodWithCause();
        
    }
    
    public class RemoteImpl implements RemoteInterface {

        @Override
        public void voidMethod(String name, Long param) {
            System.out.println(name + " " + param);
        }
        
        @Override
        public Long resultMethod(Long value) {
            return value*2;
        }
        
        @Override
        public void errorMethod() throws IOException {
            throw new IOException("Checking error throw");
        }
        
        @Override
        public void errorMethodWithCause() {
            try {
                int s = 2 / 0;
            } catch (Exception e) {
                throw new RuntimeException("Checking error throw", e);
            }
        }

        
    }
    
    @Test
    public void testInvocations() {
        RedissonClient r1 = Redisson.create();
        r1.getRemoteSerivce().register(RemoteInterface.class, new RemoteImpl());
        
        RedissonClient r2 = Redisson.create();
        RemoteInterface ri = r2.getRemoteSerivce().get(RemoteInterface.class);
        
        ri.voidMethod("someName", 100L);
        assertThat(ri.resultMethod(100L)).isEqualTo(200);

        try {
            ri.errorMethod();
            Assert.fail();
        } catch (IOException e) {
            assertThat(e.getMessage()).isEqualTo("Checking error throw");
        }
        
        try {
            ri.errorMethodWithCause();
            Assert.fail();
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(ArithmeticException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("/ by zero");
        }

        
        r1.shutdown();
        r2.shutdown();
    }
    
}
