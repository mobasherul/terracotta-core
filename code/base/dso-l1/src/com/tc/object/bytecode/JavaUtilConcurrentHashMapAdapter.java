/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */
package com.tc.object.bytecode;

import com.tc.asm.ClassAdapter;
import com.tc.asm.ClassVisitor;
import com.tc.asm.Label;
import com.tc.asm.MethodAdapter;
import com.tc.asm.MethodVisitor;
import com.tc.asm.Opcodes;
import com.tc.asm.commons.LocalVariablesSorter;

public class JavaUtilConcurrentHashMapAdapter extends ClassAdapter implements Opcodes {
  private final static String CONCURRENT_HASH_MAP_SLASH           = "java/util/concurrent/ConcurrentHashMap";
  private final static String TC_HASH_METHOD_NAME                 = ByteCodeUtil.TC_METHOD_PREFIX + "hash";
  private final static String TC_HASH_METHOD_DESC                 = "(Ljava/lang/Object;)I";
  private final static String TC_HASH_METHOD_CHECK_DESC           = "(Ljava/lang/Object;Z)I";
  private final static String TC_REHASH_METHOD_NAME               = ByteCodeUtil.TC_METHOD_PREFIX + "rehash";
  private final static String TC_REHASH_METHOD_DESC               = "()V";
  private final static String TC_CLEAR_METHOD_NAME                = ByteCodeUtil.TC_METHOD_PREFIX + "clear";
  private final static String TC_CLEAR_METHOD_DESC                = "()V";
  private final static String TC_PUT_METHOD_NAME                  = ByteCodeUtil.TC_METHOD_PREFIX + "put";
  private final static String TC_PUT_METHOD_DESC                  = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
  private final static String SEGMENT_TC_PUT_METHOD_DESC          = "(Ljava/lang/Object;ILjava/lang/Object;Z)Ljava/lang/Object;";
  private final static String TC_IS_DSO_HASH_REQUIRED_METHOD_NAME = ByteCodeUtil.TC_METHOD_PREFIX + "isDsoHashRequired";
  private final static String TC_IS_DSO_HASH_REQUIRED_METHOD_DESC = "(Ljava/lang/Object;)Z";

  public JavaUtilConcurrentHashMapAdapter(ClassVisitor cv) {
    super(cv);
  }

  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if ("entrySet".equals(name) && "()Ljava/util/Set;".equals(desc)) {
      return new EntrySetMethodAdapter(mv);
    } else if ("segmentFor".equals(name) && "(I)Ljava/util/concurrent/ConcurrentHashMap$Segment;".equals(desc)) {
      rewriteSegmentForMethod(mv);
    } else if ("containsKey".equals(name) && "(Ljava/lang/Object;)Z".equals(desc)) {
      mv = new ContainsKeyMethodAdapter(mv);
    } else if ("get".equals(name) && "(Ljava/lang/Object;)Ljava/lang/Object;".equals(desc)) {
      mv = new GetMethodAdapter(mv);
    } else if ("remove".equals(name) && "(Ljava/lang/Object;)Ljava/lang/Object;".equals(desc)) {
      mv = new SimpleRemoveMethodAdapter(mv);
    } else if ("remove".equals(name) && "(Ljava/lang/Object;Ljava/lang/Object;)Z".equals(desc)) {
      mv = new RemoveMethodAdapter(mv);
    } else if ("replace".equals(name) && "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;".equals(desc)) {
      mv = new SimpleReplaceMethodAdapter(mv);
    } else if ("replace".equals(name) && "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z".equals(desc)) {
      mv = new ReplaceMethodAdapter(mv);
    } else if ("size".equals(name) && "()I".equals(desc)) {
      rewriteSizeMethod(mv);
    } else if ("isEmpty".equals(name) && "()Z".equals(desc)) {
      rewriteIsEmpty(mv);
    }
    
    return new ConcurrentHashMapMethodAdapter(access, desc, mv);
  }

  public void visitEnd() {
    createTCPutMethod();
    createTCSharedHashMethod();
    createTCForcedHashMethod();
    createTCDsoRequiredMethod();
    createTCRehashAndSupportMethods();
    super.visitEnd();
  }
  
  private void rewriteIsEmpty(MethodVisitor mv) {
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(675, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap", "segments", "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ASTORE, 1);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLineNumber(682, l1);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitIntInsn(NEWARRAY, T_INT);
    mv.visitVarInsn(ASTORE, 2);
    Label l2 = new Label();
    mv.visitLabel(l2);
    mv.visitLineNumber(683, l2);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 3);
    Label l3 = new Label();
    mv.visitLabel(l3);
    mv.visitLineNumber(684, l3);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 4);
    Label l4 = new Label();
    mv.visitLabel(l4);
    Label l5 = new Label();
    mv.visitJumpInsn(GOTO, l5);
    Label l6 = new Label();
    mv.visitLabel(l6);
    mv.visitLineNumber(685, l6);
    mv.visitFieldInsn(GETSTATIC, "com/tc/util/DebugUtil", "DEBUG", "Z");
    Label l7 = new Label();
    mv.visitJumpInsn(IFEQ, l7);
    Label l8 = new Label();
    mv.visitLabel(l8);
    mv.visitLineNumber(686, l8);
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
    mv.visitInsn(DUP);
    mv.visitLdcInsn("Node id: ");
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
    mv.visitMethodInsn(INVOKESTATIC, "com/tc/object/bytecode/ManagerUtil", "getClientID", "()Ljava/lang/String;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    mv.visitLdcInsn(" segment ");
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    mv.visitVarInsn(ILOAD, 4);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
    mv.visitLdcInsn(".count: ");
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "count", "I");
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    mv.visitLabel(l7);
    mv.visitLineNumber(688, l7);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "count", "I");
    Label l9 = new Label();
    mv.visitJumpInsn(IFEQ, l9);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(l9);
    mv.visitLineNumber(689, l9);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "modCount", "I");
    mv.visitInsn(DUP_X2);
    mv.visitInsn(IASTORE);
    mv.visitInsn(IADD);
    mv.visitVarInsn(ISTORE, 3);
    Label l10 = new Label();
    mv.visitLabel(l10);
    mv.visitLineNumber(684, l10);
    mv.visitIincInsn(4, 1);
    mv.visitLabel(l5);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l6);
    Label l11 = new Label();
    mv.visitLabel(l11);
    mv.visitLineNumber(694, l11);
    mv.visitVarInsn(ILOAD, 3);
    Label l12 = new Label();
    mv.visitJumpInsn(IFEQ, l12);
    Label l13 = new Label();
    mv.visitLabel(l13);
    mv.visitLineNumber(695, l13);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 4);
    Label l14 = new Label();
    mv.visitLabel(l14);
    Label l15 = new Label();
    mv.visitJumpInsn(GOTO, l15);
    Label l16 = new Label();
    mv.visitLabel(l16);
    mv.visitLineNumber(696, l16);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "count", "I");
    Label l17 = new Label();
    mv.visitJumpInsn(IFNE, l17);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(IALOAD);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "modCount", "I");
    Label l18 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l18);
    mv.visitLabel(l17);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(l18);
    mv.visitLineNumber(695, l18);
    mv.visitIincInsn(4, 1);
    mv.visitLabel(l15);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l16);
    mv.visitLabel(l12);
    mv.visitLineNumber(699, l12);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);
    Label l19 = new Label();
    mv.visitLabel(l19);
    mv.visitLocalVariable("this", "Ljava/util/concurrent/ConcurrentHashMap;", "Ljava/util/concurrent/ConcurrentHashMap<TK;TV;>;", l0, l19, 0);
    mv.visitLocalVariable("segments", "[Ljava/util/concurrent/ConcurrentHashMap$Segment;", null, l1, l19, 1);
    mv.visitLocalVariable("mc", "[I", null, l2, l19, 2);
    mv.visitLocalVariable("mcsum", "I", null, l3, l19, 3);
    mv.visitLocalVariable("i", "I", null, l4, l11, 4);
    mv.visitLocalVariable("i", "I", null, l14, l12, 4);
    mv.visitMaxs(5, 5);
    mv.visitEnd();
  }
  
  private void rewriteSizeMethod(MethodVisitor mv) {
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap", "segments", "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ASTORE, 1);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitInsn(LCONST_0);
    mv.visitVarInsn(LSTORE, 2);
    Label l2 = new Label();
    mv.visitLabel(l2);
    mv.visitInsn(LCONST_0);
    mv.visitVarInsn(LSTORE, 4);
    Label l3 = new Label();
    mv.visitLabel(l3);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitIntInsn(NEWARRAY, T_INT);
    mv.visitVarInsn(ASTORE, 6);
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 7);
    Label l5 = new Label();
    mv.visitLabel(l5);
    Label l6 = new Label();
    mv.visitJumpInsn(GOTO, l6);
    Label l7 = new Label();
    mv.visitLabel(l7);
    mv.visitInsn(LCONST_0);
    mv.visitVarInsn(LSTORE, 4);
    Label l8 = new Label();
    mv.visitLabel(l8);
    mv.visitInsn(LCONST_0);
    mv.visitVarInsn(LSTORE, 2);
    Label l9 = new Label();
    mv.visitLabel(l9);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 8);
    Label l10 = new Label();
    mv.visitLabel(l10);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 9);
    Label l11 = new Label();
    mv.visitLabel(l11);
    Label l12 = new Label();
    mv.visitJumpInsn(GOTO, l12);
    Label l13 = new Label();
    mv.visitLabel(l13);
    mv.visitVarInsn(LLOAD, 2);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "count", "I");
    mv.visitInsn(I2L);
    mv.visitInsn(LADD);
    mv.visitVarInsn(LSTORE, 2);
    Label l16 = new Label();
    mv.visitLabel(l16);
    mv.visitVarInsn(ILOAD, 8);
    mv.visitVarInsn(ALOAD, 6);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "modCount", "I");
    mv.visitInsn(DUP_X2);
    mv.visitInsn(IASTORE);
    mv.visitInsn(IADD);
    mv.visitVarInsn(ISTORE, 8);
    Label l17 = new Label();
    mv.visitLabel(l17);
    mv.visitIincInsn(9, 1);
    mv.visitLabel(l12);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l13);
    Label l18 = new Label();
    mv.visitLabel(l18);
    mv.visitVarInsn(ILOAD, 8);
    Label l19 = new Label();
    mv.visitJumpInsn(IFEQ, l19);
    Label l20 = new Label();
    mv.visitLabel(l20);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 9);
    Label l21 = new Label();
    mv.visitLabel(l21);
    Label l22 = new Label();
    mv.visitJumpInsn(GOTO, l22);
    Label l23 = new Label();
    mv.visitLabel(l23);
    mv.visitVarInsn(LLOAD, 4);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "count", "I");
    mv.visitInsn(I2L);
    mv.visitInsn(LADD);
    mv.visitVarInsn(LSTORE, 4);
    Label l24 = new Label();
    mv.visitLabel(l24);
    mv.visitVarInsn(ALOAD, 6);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitInsn(IALOAD);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "modCount", "I");
    Label l25 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l25);
    Label l26 = new Label();
    mv.visitLabel(l26);
    mv.visitLdcInsn(new Long(-1L));
    mv.visitVarInsn(LSTORE, 4);
    Label l27 = new Label();
    mv.visitLabel(l27);
    mv.visitJumpInsn(GOTO, l19);
    mv.visitLabel(l25);
    mv.visitIincInsn(9, 1);
    mv.visitLabel(l22);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l23);
    mv.visitLabel(l19);
    mv.visitVarInsn(LLOAD, 4);
    mv.visitVarInsn(LLOAD, 2);
    mv.visitInsn(LCMP);
    Label l28 = new Label();
    mv.visitJumpInsn(IFNE, l28);
    Label l29 = new Label();
    mv.visitLabel(l29);
    Label l30 = new Label();
    mv.visitJumpInsn(GOTO, l30);
    mv.visitLabel(l28);
    mv.visitIincInsn(7, 1);
    mv.visitLabel(l6);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitInsn(ICONST_2);
    mv.visitJumpInsn(IF_ICMPLT, l7);
    mv.visitLabel(l30);
    mv.visitVarInsn(LLOAD, 4);
    mv.visitVarInsn(LLOAD, 2);
    mv.visitInsn(LCMP);
    Label l31 = new Label();
    mv.visitJumpInsn(IFEQ, l31);
    Label l32 = new Label();
    mv.visitLabel(l32);
    mv.visitInsn(LCONST_0);
    mv.visitVarInsn(LSTORE, 2);
    Label l33 = new Label();
    mv.visitLabel(l33);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 7);
    Label l34 = new Label();
    mv.visitLabel(l34);
    Label l35 = new Label();
    mv.visitJumpInsn(GOTO, l35);
    Label l36 = new Label();
    mv.visitLabel(l36);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitInsn(AALOAD);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", "lock", "()V");
    Label l37 = new Label();
    mv.visitLabel(l37);
    mv.visitIincInsn(7, 1);
    mv.visitLabel(l35);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l36);
    Label l38 = new Label();
    mv.visitLabel(l38);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 7);
    Label l39 = new Label();
    mv.visitLabel(l39);
    Label l40 = new Label();
    mv.visitJumpInsn(GOTO, l40);
    Label l41 = new Label();
    mv.visitLabel(l41);
    mv.visitVarInsn(LLOAD, 2);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "count", "I");
    mv.visitInsn(I2L);
    mv.visitInsn(LADD);
    mv.visitVarInsn(LSTORE, 2);
    Label l42 = new Label();
    mv.visitLabel(l42);
    mv.visitIincInsn(7, 1);
    mv.visitLabel(l40);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l41);
    Label l45 = new Label();
    mv.visitLabel(l45);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 7);
    Label l46 = new Label();
    mv.visitLabel(l46);
    Label l47 = new Label();
    mv.visitJumpInsn(GOTO, l47);
    Label l48 = new Label();
    mv.visitLabel(l48);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitInsn(AALOAD);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", "unlock", "()V");
    Label l49 = new Label();
    mv.visitLabel(l49);
    mv.visitIincInsn(7, 1);
    mv.visitLabel(l47);
    mv.visitVarInsn(ILOAD, 7);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l48);
    mv.visitLabel(l31);
    mv.visitVarInsn(LLOAD, 2);
    mv.visitLdcInsn(new Long(2147483647L));
    mv.visitInsn(LCMP);
    Label l50 = new Label();
    mv.visitJumpInsn(IFLE, l50);
    Label l51 = new Label();
    mv.visitLabel(l51);
    mv.visitLdcInsn(new Integer(2147483647));
    mv.visitInsn(IRETURN);
    mv.visitLabel(l50);
    mv.visitVarInsn(LLOAD, 2);
    mv.visitInsn(L2I);
    mv.visitInsn(IRETURN);
    Label l52 = new Label();
    mv.visitLabel(l52);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void rewriteSegmentForMethod(MethodVisitor mv) {
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ILOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I");
    mv.visitVarInsn(ISTORE, 1);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitInsn(ARRAYLENGTH);
    mv.visitInsn(IREM);
    mv.visitInsn(AALOAD);
    mv.visitInsn(ARETURN);
    Label l2 = new Label();
    mv.visitLabel(l2);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void createTCDsoRequiredMethod() {
    MethodVisitor mv = super.visitMethod(ACC_PRIVATE, TC_IS_DSO_HASH_REQUIRED_METHOD_NAME, TC_IS_DSO_HASH_REQUIRED_METHOD_DESC, null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap", "__tc_managed",
                       "()Lcom/tc/object/TCObject;");
    Label l1 = new Label();
    mv.visitJumpInsn(IFNULL, l1);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(INSTANCEOF, "com/tc/object/bytecode/Manageable");
    Label l2 = new Label();
    mv.visitJumpInsn(IFEQ, l2);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, "com/tc/object/bytecode/Manageable");
    mv.visitMethodInsn(INVOKEINTERFACE, "com/tc/object/bytecode/Manageable", "__tc_managed",
                       "()Lcom/tc/object/TCObject;");
    mv.visitJumpInsn(IFNONNULL, l1);
    mv.visitLabel(l2);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I");
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I");
    mv.visitJumpInsn(IF_ICMPNE, l1);
    Label l3 = new Label();
    mv.visitLabel(l3);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);
    mv.visitLabel(l1);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void createTCRehashAndSupportMethods() {
    createTCRehashMethod();
    createTCClearMethod();
  }

  private void createTCRehashMethod() {
    MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_SYNTHETIC, TC_REHASH_METHOD_NAME, TC_REHASH_METHOD_DESC,
                                         null, null);
    mv.visitCode();
    Label l0 = new Label();
    Label l1 = new Label();
    mv.visitTryCatchBlock(l0, l1, l1, null);
    Label l2 = new Label();
    Label l3 = new Label();
    mv.visitTryCatchBlock(l2, l3, l1, null);
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKEVIRTUAL, CONCURRENT_HASH_MAP_SLASH, "size", "()I");
    mv.visitJumpInsn(IFLE, l3);
    Label l5 = new Label();
    mv.visitLabel(l5);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 1);
    Label l6 = new Label();
    mv.visitLabel(l6);
    Label l7 = new Label();
    mv.visitJumpInsn(GOTO, l7);
    Label l8 = new Label();
    mv.visitLabel(l8);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ILOAD, 1);
    mv.visitInsn(AALOAD);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", "lock", "()V");
    Label l9 = new Label();
    mv.visitLabel(l9);
    mv.visitIincInsn(1, 1);
    mv.visitLabel(l7);
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l8);
    mv.visitLabel(l0);
    mv.visitTypeInsn(NEW, "java/util/ArrayList");
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
    mv.visitVarInsn(ASTORE, 1);
    Label l10 = new Label();
    mv.visitLabel(l10);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 2);
    Label l11 = new Label();
    mv.visitLabel(l11);
    Label l12 = new Label();
    mv.visitJumpInsn(GOTO, l12);
    Label l13 = new Label();
    mv.visitLabel(l13);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ILOAD, 2);
    mv.visitInsn(AALOAD);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$Segment", "table",
                      "[Ljava/util/concurrent/ConcurrentHashMap$HashEntry;");
    mv.visitVarInsn(ASTORE, 3);
    Label l14 = new Label();
    mv.visitLabel(l14);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 4);
    Label l15 = new Label();
    mv.visitLabel(l15);
    Label l16 = new Label();
    mv.visitJumpInsn(GOTO, l16);
    Label l17 = new Label();
    mv.visitLabel(l17);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    Label l18 = new Label();
    mv.visitJumpInsn(IFNULL, l18);
    Label l19 = new Label();
    mv.visitLabel(l19);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitInsn(AALOAD);
    mv.visitVarInsn(ASTORE, 5);
    Label l20 = new Label();
    mv.visitLabel(l20);
    Label l21 = new Label();
    mv.visitJumpInsn(GOTO, l21);
    Label l22 = new Label();
    mv.visitLabel(l22);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
    mv.visitInsn(POP);
    Label l23 = new Label();
    mv.visitLabel(l23);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$HashEntry", "next",
                      "Ljava/util/concurrent/ConcurrentHashMap$HashEntry;");
    mv.visitVarInsn(ASTORE, 5);
    mv.visitLabel(l21);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitJumpInsn(IFNONNULL, l22);
    mv.visitLabel(l18);
    mv.visitIincInsn(4, 1);
    mv.visitLabel(l16);
    mv.visitVarInsn(ILOAD, 4);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l17);
    Label l24 = new Label();
    mv.visitLabel(l24);
    mv.visitIincInsn(2, 1);
    mv.visitLabel(l12);
    mv.visitVarInsn(ILOAD, 2);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l13);
    Label l25 = new Label();
    mv.visitLabel(l25);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKEVIRTUAL, CONCURRENT_HASH_MAP_SLASH, TC_CLEAR_METHOD_NAME, TC_CLEAR_METHOD_DESC);
    Label l26 = new Label();
    mv.visitLabel(l26);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;");
    mv.visitVarInsn(ASTORE, 2);
    Label l27 = new Label();
    mv.visitLabel(l27);
    Label l28 = new Label();
    mv.visitJumpInsn(GOTO, l28);
    Label l29 = new Label();
    mv.visitLabel(l29);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
    mv.visitTypeInsn(CHECKCAST, "java/util/concurrent/ConcurrentHashMap$HashEntry");
    mv.visitVarInsn(ASTORE, 3);
    Label l30 = new Label();
    mv.visitLabel(l30);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$HashEntry", "key", "Ljava/lang/Object;");
    mv.visitVarInsn(ASTORE, 4);
    Label l31 = new Label();
    mv.visitLabel(l31);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitFieldInsn(GETFIELD, "java/util/concurrent/ConcurrentHashMap$HashEntry", "value", "Ljava/lang/Object;");
    mv.visitVarInsn(ASTORE, 5);
    Label l32 = new Label();
    mv.visitLabel(l32);
    mv.visitVarInsn(ALOAD, 4);
    mv.visitMethodInsn(INVOKESTATIC, CONCURRENT_HASH_MAP_SLASH, "hash", "(Ljava/lang/Object;)I");
    mv.visitVarInsn(ISTORE, 6);
    Label l33 = new Label();
    mv.visitLabel(l33);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 4);
    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKESPECIAL, CONCURRENT_HASH_MAP_SLASH, TC_HASH_METHOD_NAME, TC_HASH_METHOD_CHECK_DESC);
    mv.visitMethodInsn(INVOKEVIRTUAL, CONCURRENT_HASH_MAP_SLASH, "segmentFor",
                       "(I)Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ALOAD, 4);
    mv.visitVarInsn(ILOAD, 6);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", TC_PUT_METHOD_NAME,
                       SEGMENT_TC_PUT_METHOD_DESC);
    mv.visitInsn(POP);
    mv.visitLabel(l28);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
    mv.visitJumpInsn(IFNE, l29);
    Label l34 = new Label();
    mv.visitLabel(l34);
    mv.visitJumpInsn(GOTO, l2);
    mv.visitLabel(l1);
    mv.visitVarInsn(ASTORE, 8);
    Label l35 = new Label();
    mv.visitJumpInsn(JSR, l35);
    Label l36 = new Label();
    mv.visitLabel(l36);
    mv.visitVarInsn(ALOAD, 8);
    mv.visitInsn(ATHROW);
    mv.visitLabel(l35);
    mv.visitVarInsn(ASTORE, 7);
    Label l37 = new Label();
    mv.visitLabel(l37);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 9);
    Label l38 = new Label();
    mv.visitLabel(l38);
    Label l39 = new Label();
    mv.visitJumpInsn(GOTO, l39);
    Label l40 = new Label();
    mv.visitLabel(l40);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ILOAD, 9);
    mv.visitInsn(AALOAD);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", "unlock", "()V");
    Label l41 = new Label();
    mv.visitLabel(l41);
    mv.visitIincInsn(9, 1);
    mv.visitLabel(l39);
    mv.visitVarInsn(ILOAD, 9);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l40);
    Label l42 = new Label();
    mv.visitLabel(l42);
    mv.visitVarInsn(RET, 7);
    mv.visitLabel(l2);
    mv.visitJumpInsn(JSR, l35);
    mv.visitLabel(l3);
    mv.visitInsn(RETURN);
    Label l43 = new Label();
    mv.visitLabel(l43);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void createTCPutMethod() {
    MethodVisitor mv = super
        .visitMethod(ACC_PUBLIC + ACC_SYNTHETIC, TC_PUT_METHOD_NAME, TC_PUT_METHOD_DESC, null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 2);
    Label l1 = new Label();
    mv.visitJumpInsn(IFNONNULL, l1);
    mv.visitTypeInsn(NEW, "java/lang/NullPointerException");
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "()V");
    mv.visitInsn(ATHROW);
    mv.visitLabel(l1);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, "java/util/concurrent/ConcurrentHashMap", "hash", "(Ljava/lang/Object;)I");
    mv.visitVarInsn(ISTORE, 3);
    Label l2 = new Label();
    mv.visitLabel(l2);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/ConcurrentHashMap", TC_HASH_METHOD_NAME,
                       TC_HASH_METHOD_CHECK_DESC);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap", "segmentFor",
                       "(I)Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitInsn(ICONST_0);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", TC_PUT_METHOD_NAME,
                       "(Ljava/lang/Object;ILjava/lang/Object;Z)Ljava/lang/Object;");
    mv.visitInsn(ARETURN);
    Label l3 = new Label();
    mv.visitLabel(l3);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void createTCClearMethod() {
    MethodVisitor mv = super.visitMethod(ACC_PRIVATE + ACC_SYNTHETIC, TC_CLEAR_METHOD_NAME, TC_CLEAR_METHOD_DESC, null,
                                         null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 1);
    Label l1 = new Label();
    mv.visitLabel(l1);
    Label l2 = new Label();
    mv.visitJumpInsn(GOTO, l2);
    Label l3 = new Label();
    mv.visitLabel(l3);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitVarInsn(ILOAD, 1);
    mv.visitInsn(AALOAD);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap$Segment", TC_CLEAR_METHOD_NAME,
                       TC_CLEAR_METHOD_DESC);
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitIincInsn(1, 1);
    mv.visitLabel(l2);
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, CONCURRENT_HASH_MAP_SLASH, "segments",
                      "[Ljava/util/concurrent/ConcurrentHashMap$Segment;");
    mv.visitInsn(ARRAYLENGTH);
    mv.visitJumpInsn(IF_ICMPLT, l3);
    Label l5 = new Label();
    mv.visitLabel(l5);
    mv.visitInsn(RETURN);
    Label l6 = new Label();
    mv.visitLabel(l6);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  /*
   * ConcurrentHashMap uses the hashcode of the key and identify the segment to use. Each
   * segment is an ReentrantLock. This prevents multiple threads to update the same segment
   * at the same time.
   * 
   * To support in DSO, we need to check if the ConcurrentHashMap is a shared object. If it
   * is, we check if the hashcode of the key is the same as the System.identityHashCode. If
   * it is, we will use the DSO ObjectID of the key to be the hashcode. Since the ObjectID
   * of the key is a cluster-wide constant, different node will identify the same segment
   * based on the ObjectID of the key.
   * 
   * If the hashcode of the key is not the same as the System.identityHashCode, that would
   * mean the application has defined the hashcode of the key and in this case, we could
   * use honor the application defined hashcode of the key.
   * 
   * The reason that we do not want to always use the ObjectID of the key is because if
   * the application has defined the hashcode of the key, map.get(key1) and map.get(key2)
   * will return the same object if key1 and key2 has the same application defined hashcode
   * even though key1 and key2 has 2 different ObjectID. Using ObjectID as the hashcode in
   * this case will prevent map.get(key1) and map.get(key2) to return the same result.
   * 
   * If the application has not defined the hashcode of the key, key1 and key2 will have
   * 2 different hashcode (due to the fact that they will have different System.identityHashCode).
   * Therefore, map.get(key1) and map.get(key2) will return different objects. In this case,
   * using ObjectID will have the proper behavior.
   * 
   * One limitation is that if the application define the hashcode as some combination of
   * system specific data such as a combination of System.identityHashCode() and some other
   * data, the current support of ConcurrentHashMap does not support this scenario.
   * 
   * Another limitation is that if the application defined hashcode of the key happens to
   * be the same as the System.identityHashCode, the current support of ConcurrentHashMap
   * does not support this scenario either.
   */
  private void createTCSharedHashMethod() {
    MethodVisitor mv = cv
        .visitMethod(ACC_PRIVATE + ACC_SYNTHETIC, TC_HASH_METHOD_NAME, TC_HASH_METHOD_DESC, null, null);
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitInsn(ICONST_1);
    mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/ConcurrentHashMap", TC_HASH_METHOD_NAME,
                       TC_HASH_METHOD_CHECK_DESC);
    mv.visitInsn(IRETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private void createTCForcedHashMethod() {
    MethodVisitor mv = super.visitMethod(ACC_PRIVATE + ACC_SYNTHETIC, TC_HASH_METHOD_NAME, TC_HASH_METHOD_CHECK_DESC,
                                         null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I");
    mv.visitVarInsn(ISTORE, 3);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 4);
    Label l2 = new Label();
    mv.visitLabel(l2);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I");
    mv.visitVarInsn(ILOAD, 3);
    Label l3 = new Label();
    mv.visitJumpInsn(IF_ICMPNE, l3);
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitVarInsn(ILOAD, 2);
    Label l5 = new Label();
    mv.visitJumpInsn(IFEQ, l5);
    Label l6 = new Label();
    mv.visitLabel(l6);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap", "__tc_managed",
                       "()Lcom/tc/object/TCObject;");
    Label l7 = new Label();
    mv.visitJumpInsn(IFNONNULL, l7);
    mv.visitMethodInsn(INVOKESTATIC, "com/tc/object/bytecode/ManagerUtil", "isCreationInProgress", "()Z");
    mv.visitJumpInsn(IFEQ, l3);
    mv.visitLabel(l7);
    mv.visitInsn(ICONST_1);
    mv.visitVarInsn(ISTORE, 4);
    mv.visitJumpInsn(GOTO, l3);
    mv.visitLabel(l5);
    mv.visitInsn(ICONST_1);
    mv.visitVarInsn(ISTORE, 4);
    mv.visitLabel(l3);
    mv.visitVarInsn(ILOAD, 4);
    Label l8 = new Label();
    mv.visitJumpInsn(IFEQ, l8);
    Label l9 = new Label();
    mv.visitLabel(l9);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESTATIC, "com/tc/object/bytecode/ManagerUtil", "shareObjectIfNecessary",
                       "(Ljava/lang/Object;)Lcom/tc/object/TCObject;");
    mv.visitVarInsn(ASTORE, 5);
    Label l10 = new Label();
    mv.visitLabel(l10);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitJumpInsn(IFNULL, l8);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitMethodInsn(INVOKEINTERFACE, "com/tc/object/TCObject", "getObjectID", "()Lcom/tc/object/ObjectID;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "com/tc/object/ObjectID", "hashCode", "()I");
    mv.visitVarInsn(ISTORE, 3);
    mv.visitLabel(l8);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitIntInsn(BIPUSH, 9);
    mv.visitInsn(ISHL);
    mv.visitInsn(ICONST_M1);
    mv.visitInsn(IXOR);
    mv.visitInsn(IADD);
    mv.visitVarInsn(ISTORE, 3);
    Label l11 = new Label();
    mv.visitLabel(l11);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitIntInsn(BIPUSH, 14);
    mv.visitInsn(IUSHR);
    mv.visitInsn(IXOR);
    mv.visitVarInsn(ISTORE, 3);
    Label l12 = new Label();
    mv.visitLabel(l12);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitInsn(ICONST_4);
    mv.visitInsn(ISHL);
    mv.visitInsn(IADD);
    mv.visitVarInsn(ISTORE, 3);
    Label l13 = new Label();
    mv.visitLabel(l13);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitIntInsn(BIPUSH, 10);
    mv.visitInsn(IUSHR);
    mv.visitInsn(IXOR);
    mv.visitVarInsn(ISTORE, 3);
    Label l14 = new Label();
    mv.visitLabel(l14);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitInsn(IRETURN);
    Label l17 = new Label();
    mv.visitLabel(l17);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static class EntrySetMethodAdapter extends MethodAdapter implements Opcodes {

    public EntrySetMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      super.visitMethodInsn(opcode, owner, name, desc);

      if ((opcode == INVOKESPECIAL) && "java/util/concurrent/ConcurrentHashMap$EntrySet".equals(owner)
          && "<init>".equals(name) && "(Ljava/util/concurrent/ConcurrentHashMap;)V".equals(desc)) {
        mv.visitVarInsn(ASTORE, 1);
        mv.visitTypeInsn(NEW, "com/tcclient/util/ConcurrentHashMapEntrySetWrapper");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, "com/tcclient/util/ConcurrentHashMapEntrySetWrapper", "<init>",
                           "(Ljava/util/Map;Ljava/util/Set;)V");
      }
    }
  }

  private abstract static class AddCheckManagedKeyMethodAdapter extends MethodAdapter implements Opcodes {
    public AddCheckManagedKeyMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    public void visitCode() {
      super.visitCode();
      addCheckManagedKeyCode();
    }

    protected abstract void addCheckManagedKeyCode();
  }
  
  private static class ContainsKeyMethodAdapter extends AddCheckManagedKeyMethodAdapter {
    public ContainsKeyMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    protected void addCheckManagedKeyCode() {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/ConcurrentHashMap", TC_IS_DSO_HASH_REQUIRED_METHOD_NAME, TC_IS_DSO_HASH_REQUIRED_METHOD_DESC);
      Label l1 = new Label();
      mv.visitJumpInsn(IFNE, l1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitInsn(ICONST_0);
      mv.visitInsn(IRETURN);
      mv.visitLabel(l1);
    }
  }

  private static class GetMethodAdapter extends AddCheckManagedKeyMethodAdapter {
    public GetMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    protected void addCheckManagedKeyCode() {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/ConcurrentHashMap", TC_IS_DSO_HASH_REQUIRED_METHOD_NAME, TC_IS_DSO_HASH_REQUIRED_METHOD_DESC);
      Label l1 = new Label();
      mv.visitJumpInsn(IFNE, l1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitInsn(ACONST_NULL);
      mv.visitInsn(ARETURN);
      mv.visitLabel(l1);
    }
  }

  private static class SimpleRemoveMethodAdapter extends GetMethodAdapter {
    public SimpleRemoveMethodAdapter(MethodVisitor mv) {
      super(mv);
    }
  }

  private static class SimpleReplaceMethodAdapter extends SimpleRemoveMethodAdapter {
    private Label target;

    public SimpleReplaceMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    public void visitCode() {
      mv.visitCode();
    }

    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      if (IFNONNULL == opcode) {
        target = label;
      }
    }

    public void visitLabel(Label label) {
      super.visitLabel(label);
      if (label.equals(target)) {
        addCheckManagedKeyCode();
      }
    }
  }

  private static class RemoveMethodAdapter extends AddCheckManagedKeyMethodAdapter {
    public RemoveMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    protected void addCheckManagedKeyCode() {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/ConcurrentHashMap", TC_IS_DSO_HASH_REQUIRED_METHOD_NAME, TC_IS_DSO_HASH_REQUIRED_METHOD_DESC);
      Label l1 = new Label();
      mv.visitJumpInsn(IFNE, l1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitInsn(ICONST_0);
      mv.visitInsn(IRETURN);
      mv.visitLabel(l1);
    }
  }

  private static class ReplaceMethodAdapter extends RemoveMethodAdapter {
    private Label target;

    public ReplaceMethodAdapter(MethodVisitor mv) {
      super(mv);
    }

    public void visitCode() {
      mv.visitCode();
    }

    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      if (IFNONNULL == opcode) {
        target = label;
      }
    }

    public void visitLabel(Label label) {
      super.visitLabel(label);
      if (label.equals(target)) {
        addCheckManagedKeyCode();
      }
    }
  }

  private static class ConcurrentHashMapMethodAdapter extends LocalVariablesSorter implements Opcodes {

    public ConcurrentHashMapMethodAdapter(int access, String desc, MethodVisitor mv) {
      super(access, desc, mv);
    }

    public int newLocal(int size) {
      return super.newLocal(size);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      if (INVOKEVIRTUAL == opcode && CONCURRENT_HASH_MAP_SLASH.equals(owner) && "segmentFor".equals(name)
          && "(I)Ljava/util/concurrent/ConcurrentHashMap$Segment;".equals(desc)) {
        mv.visitInsn(POP);
        ByteCodeUtil.pushThis(mv);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, owner, TC_HASH_METHOD_NAME, TC_HASH_METHOD_DESC);
        super.visitMethodInsn(opcode, owner, name, desc);
      } else if (INVOKESPECIAL == opcode
                 && JavaUtilConcurrentHashMapSegmentAdapter.CONCURRENT_HASH_MAP_SEGMENT_SLASH.equals(owner)
                 && "<init>".equals(name) && "(IF)V".equals(desc)) {
        mv.visitInsn(POP);
        mv.visitInsn(POP);
        ByteCodeUtil.pushThis(mv);
        mv.visitVarInsn(ILOAD, 7);
        mv.visitVarInsn(FLOAD, 2);
        mv.visitMethodInsn(opcode, owner, name, JavaUtilConcurrentHashMapSegmentAdapter.INIT_DESC);
      } else {
        super.visitMethodInsn(opcode, owner, name, desc);
      }
    }
  }
}