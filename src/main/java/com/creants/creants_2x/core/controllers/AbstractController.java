package com.creants.creants_2x.core.controllers;

import com.creants.creants_2x.socket.io.IRequest;

/**
 * @author LamHa
 *
 */
public abstract class AbstractController implements IController {
	protected Object id;
	protected String name;


	public AbstractController() {
	}


	@Override
	public void enqueueRequest(IRequest request) throws Exception {
		processRequest(request);
	}


	@Override
	public void init(Object o) {
	}


	@Override
	public void destroy(final Object o) {
	}


	@Override
	public void handleMessage(final Object message) {
	}


	public abstract void processRequest(IRequest request) throws Exception;


	@Override
	public Object getId() {
		return id;
	}


	@Override
	public void setId(final Object id) {
		this.id = id;
	}


	@Override
	public String getName() {
		return this.name;
	}


	@Override
	public void setName(String name) {
		this.name = name;
	}

}
