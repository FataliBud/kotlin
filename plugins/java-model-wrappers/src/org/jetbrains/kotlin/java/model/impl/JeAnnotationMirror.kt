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

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTypesUtil
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType

class JeAnnotationMirror(val psi: PsiAnnotation) : AnnotationMirror {
    override fun getAnnotationType(): DeclaredType? {
        val psiClass = psi.reference?.resolve() as? PsiClass ?: return JeDeclaredErrorType
        return JeDeclaredType(PsiTypesUtil.getClassType(psiClass))
    }

    override fun getElementValues(): Map<out ExecutableElement, AnnotationValue> {
        throw UnsupportedOperationException()
    }
    
    fun getAllElementValues(): Map<out ExecutableElement, AnnotationValue> {
        throw UnsupportedOperationException()   
    }
}