package com.github.soniex2.notebetter.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author soniex2
 */
public class NoteBetterClassTransformer implements IClassTransformer {
    private Map<String, String> maps;
    private boolean isObfuscated;

    private void setupDeobfMaps() {
        maps = new HashMap<String, String>();
        maps.put("te_mname", "triggerNote");
        maps.put("te_mdesc", "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V");
        maps.put("nm_te_hndlr_desc", "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)Z");
    }

    private void setupObfMaps() {
        maps = new HashMap<String, String>();
        maps.put("te_mname", "func_175108_a");
        maps.put("te_mdesc", "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V");
        maps.put("nm_te_hndlr_desc", "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)Z");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        isObfuscated = !name.equals(transformedName);
        if (transformedName.equals("net.minecraft.tileentity.TileEntityNote"))
            return transformTileEntityNote(basicClass);
        return basicClass;
    }

    private byte[] transformTileEntityNote(byte[] basicClass) {
        if (isObfuscated) setupObfMaps();
        else setupDeobfMaps();

        System.out.println("Transforming TileEntityNote");

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            boolean transformed = false;

            for (MethodNode m : classNode.methods) {
                if (m.name.equals(maps.get("te_mname")) && m.desc.equals(maps.get("te_mdesc"))) {
                    ListIterator<AbstractInsnNode> it = m.instructions.iterator();
                    System.out.println("Transforming TileEntityNote.triggerNote");
                    VarInsnNode vigetWorld = new VarInsnNode(Opcodes.ALOAD, 1);
                    it.add(vigetWorld);
                    VarInsnNode vigetPos = new VarInsnNode(Opcodes.ALOAD, 2);
                    it.add(vigetPos);
                    MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "com/github/soniex2/notebetter/NoteMethods",
                            "handleTileEntity",
                            maps.get("nm_te_hndlr_desc"),
                            false);
                    it.add(call);
                    LabelNode cont = new LabelNode();
                    JumpInsnNode test = new JumpInsnNode(Opcodes.IFEQ, cont);
                    it.add(test);
                    InsnNode ret = new InsnNode(Opcodes.RETURN);
                    it.add(ret);
                    it.add(cont);
                    System.out.println("Done!");
                    transformed = true;
                }
            }
            if (!transformed)
                System.out.println("Failed to transform TileEntityNote.triggerNote");

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            if (transformed)
                System.out.println("Transforming TileEntityNote - Success!");
            else
                System.out.println("Transforming TileEntityNote - Failed!");
            return writer.toByteArray();
        } catch (Exception e) {
            System.out.println("Transforming TileEntityNote - Failed!");
            e.printStackTrace();
        }

        return basicClass;
    }
}