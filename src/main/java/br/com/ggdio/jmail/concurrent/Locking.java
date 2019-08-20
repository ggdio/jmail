package br.com.ggdio.jmail.concurrent;

/**
 * A facade for locking solutions
 * 
 * @author Guilherme Dio
 *
 */
public interface Locking {

	public boolean lock(String namespace);
	
	public void unlock(String namespace);
	
}