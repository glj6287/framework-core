package com.hywin.framework.aspect;

import com.hywin.framework.constant.Constants;
import com.hywin.framework.exception.SystemException;
import com.hywin.framework.pojo.ResponseEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author wu youyang
 */

public class ExceptionAspect
{

	private Logger logger = LogManager.getLogger(this.getClass());

	private String systemCode;

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	/**
	 * 处理运行异常的切面
	 * */
	public Object deal(ProceedingJoinPoint pjp) throws Throwable {
		Object returnMessage = null;
		Class<?> aClass = pjp.getTarget().getClass();
		logger.info(aClass.getName() + "#" + pjp.getSignature().getName() + ": request:{}", pjp.getArgs());
		try {
			returnMessage = pjp.proceed();
		} catch (IllegalArgumentException e1) {
			logger.info(e1);
			returnMessage = new ResponseEntity(this.systemCode + Constants.SYS_ERROR_CODE05, Constants.SYS_ERROR_MSG05 + e1.getMessage());
		} catch (SystemException e2) {
			logger.error(e2.getStackTrace());
			returnMessage = new ResponseEntity(this.systemCode + Constants.SYS_ERROR_CODE05, Constants.SYS_ERROR_MSG05);
		} catch (Exception e3) {
			logger.error(e3.getStackTrace());
			returnMessage = new ResponseEntity(this.systemCode + Constants.RUN_TIME_ERROR_CODE, Constants.RUN_TIME_ERROR_MSG + e3.getMessage());
		}
		logger.info(aClass.getName() + "#" + pjp.getSignature().getName() + ": response:{}", returnMessage);
		return returnMessage;
	}
}
