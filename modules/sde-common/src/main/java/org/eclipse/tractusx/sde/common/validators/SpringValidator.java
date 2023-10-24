package org.eclipse.tractusx.sde.common.validators;


import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
public class SpringValidator {
    public <T> T validate(@Valid T t) {
        return t;
    }
}
