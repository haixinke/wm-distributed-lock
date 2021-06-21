package com.wm.spring.boot.autoconfigure.lock.el;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ExpressionRootObject {

    @Getter
    private final Object object;

    @Getter
    private final Object[] args;
}
