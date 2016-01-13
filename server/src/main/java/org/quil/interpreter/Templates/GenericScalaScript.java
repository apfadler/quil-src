package org.quil.interpreter.Templates;

import org.springframework.context.ApplicationContext;
import org.apache.ignite.Ignite;

public interface GenericScalaScript {
	abstract void main(ApplicationContext context, Ignite ignite);
}
