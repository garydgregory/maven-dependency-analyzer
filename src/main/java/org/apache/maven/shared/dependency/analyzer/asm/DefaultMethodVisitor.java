/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.dependency.analyzer.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Computes the set of classes referenced by visited code.
 * Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in the ASM dependencies example.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 */
public class DefaultMethodVisitor extends MethodVisitor {
    private final AnnotationVisitor annotationVisitor;

    private final SignatureVisitor signatureVisitor;

    private final ResultCollector resultCollector;

    /**
     * <p>Constructor for DefaultMethodVisitor.</p>
     *
     * @param annotationVisitor a {@link org.objectweb.asm.AnnotationVisitor} object.
     * @param signatureVisitor a {@link org.objectweb.asm.signature.SignatureVisitor} object.
     * @param resultCollector a {@link org.apache.maven.shared.dependency.analyzer.asm.ResultCollector} object.
     */
    public DefaultMethodVisitor(
            AnnotationVisitor annotationVisitor, SignatureVisitor signatureVisitor, ResultCollector resultCollector) {
        super(Opcodes.ASM9);
        this.annotationVisitor = annotationVisitor;
        this.signatureVisitor = signatureVisitor;
        this.resultCollector = resultCollector;
    }

    /** {@inheritDoc} */
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        resultCollector.addDesc(desc);

        return annotationVisitor;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        resultCollector.addDesc(desc);

        return annotationVisitor;
    }

    /** {@inheritDoc} */
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        resultCollector.addDesc(desc);

        return annotationVisitor;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(
            int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        resultCollector.addDesc(desc);

        return annotationVisitor;
    }

    /** {@inheritDoc} */
    public void visitTypeInsn(final int opcode, final String desc) {
        if (desc.charAt(0) == '[') {
            resultCollector.addDesc(desc);
        } else {
            resultCollector.addName(desc);
        }
    }

    /** {@inheritDoc} */
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        resultCollector.addName(owner);
        /*
         * NOTE: Merely accessing a field does not impose a direct dependency on its type. For example, the code line
         * <code>java.lang.Object var = bean.field;</code> does not directly depend on the type of the field. A direct
         * dependency is only introduced when the code explicitly references the field's type by means of a variable
         * declaration or a type check/cast. Those cases are handled by other visitor callbacks.
         */
    }

    /** {@inheritDoc} */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        resultCollector.addName(owner);
    }

    /** {@inheritDoc} */
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            resultCollector.addType((Type) cst);
        }
    }

    /** {@inheritDoc} */
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        resultCollector.addDesc(desc);
    }

    /** {@inheritDoc} */
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        resultCollector.addName(type);
    }

    /** {@inheritDoc} */
    public void visitLocalVariable(
            final String name,
            final String desc,
            final String signature,
            final Label start,
            final Label end,
            final int index) {
        if (signature == null) {
            resultCollector.addDesc(desc);
        } else {
            addTypeSignature(signature);
        }
    }

    private void addTypeSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).acceptType(signatureVisitor);
        }
    }
}
