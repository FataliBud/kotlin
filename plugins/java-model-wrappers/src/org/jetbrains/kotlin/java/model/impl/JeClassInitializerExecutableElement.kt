/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.java.model.impl

import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiModifier
import org.jetbrains.kotlin.java.model.JeElement
import org.jetbrains.kotlin.java.model.JeModifierListOwner
import org.jetbrains.kotlin.java.model.JeName
import org.jetbrains.kotlin.java.model.JeNoAnnotations
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class JeClassInitializerExecutableElement(override val psi: PsiClassInitializer) : JeElement(), 
        ExecutableElement, JeNoAnnotations, JeModifierListOwner
{
    val isStaticInitializer = psi.hasModifierProperty(PsiModifier.STATIC) 
    
    override fun getSimpleName() = if (isStaticInitializer) JeName.CLINIT else JeName.EMPTY

    override fun getThrownTypes() = emptyList<TypeMirror>()

    override fun getTypeParameters() = emptyList<TypeParameterElement>()

    override fun getParameters() = emptyList<VariableElement>()

    override fun getDefaultValue() = null

    override fun getReturnType() = JeNoneType

    override fun getReceiverType() = JeNoneType

    override fun isVarArgs() = false

    override fun isDefault() = false

    override fun getKind() = if (isStaticInitializer) ElementKind.STATIC_INIT else ElementKind.INSTANCE_INIT

    override fun asType() = JeExecutableTypeMirror(psi)

    override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>, p: P) = v.visitExecutable(this, p)

    override fun getEnclosedElements() = emptyList<Element>()
}