package com.hywin.framework.pojo;

/**
 * 需要分页的实体实现此接口
 * @author zhou shengzong
 *
 */
public interface Pageable {
	/**
	 * 
	 * @param page 
	 * @return {@link #getPage()}
	 */
	Page setPage(Page page);
	Page getPage();
}
