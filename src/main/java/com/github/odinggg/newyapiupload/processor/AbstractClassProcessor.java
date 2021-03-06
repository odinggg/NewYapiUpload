package com.github.odinggg.newyapiupload.processor;

import com.github.odinggg.newyapiupload.util.PsiAnnotationSearchUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base lombok processor class for class annotations
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractClassProcessor {

    protected AbstractClassProcessor() {
    }


    @NotNull
    public Collection<PsiAnnotation> collectProcessedAnnotations(@NotNull PsiClass psiClass) {
        Collection<PsiAnnotation> result = new ArrayList<>();
        PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, "");
        if (null != psiAnnotation) {
            result.add(psiAnnotation);
        }
        return result;
    }

}
