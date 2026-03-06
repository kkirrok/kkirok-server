package com.kkirok.server.global.common.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
	@Pointcut("execution(* com.kkirok.server..*Controller.*(..))")
	public void allController() {}

	@Pointcut("execution(* com.kkirok.server..*Service.*(..)) || execution(* com.kkirok.server..*UseCase.*(..)) || execution(* com.kkirok.server..*Facade.*(..))")
	public void allService() {}

	@Pointcut("execution(* com.kkirok.server..*(..))" +
		" && !within(com.kkirok.server.global..*)")
	public void allApplicationLogic() {}
}
