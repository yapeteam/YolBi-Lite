package cn.yapeteam.agent;

import com.sun.tools.attach.*;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author yuxiangll
 * @since 2024/7/26 下午4:00
 * IntelliJ IDEA
 */
public class AgentInjector {
    public static void main(String[] args) {

        VirtualMachine.list().forEach((it)->{
            if (it.displayName().contains("net.minecraft.client.main.Main")){
                try {
                    VirtualMachine vm = VirtualMachine.attach(it.id());
                    vm.loadAgent("/Users/yuxiangll/Documents/YolBi/YolBi-Lite/build/agent.jar");
                    vm.detach();
                } catch (AgentLoadException | IOException | AttachNotSupportedException | AgentInitializationException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }
}
