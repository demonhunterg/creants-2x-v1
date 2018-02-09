package com.creants.creants_2x.core.controllers.system;

import java.util.HashMap;
import java.util.Map;

import com.creants.creants_2x.core.IQAntEventParam;
import com.creants.creants_2x.core.QAntEventParam;
import com.creants.creants_2x.core.QAntEventSysParam;
import com.creants.creants_2x.core.QAntEventType;
import com.creants.creants_2x.core.QAntSystemEvent;
import com.creants.creants_2x.core.controllers.BaseControllerCommand;
import com.creants.creants_2x.core.controllers.SystemRequest;
import com.creants.creants_2x.core.entities.Zone;
import com.creants.creants_2x.core.exception.QAntRequestValidationException;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.io.IRequest;

/**
 * @author LamHM
 *
 */
public class Login extends BaseControllerCommand {
	public static final String ZONE_NAME = "zn";
	public static final String REQUEST_LOGIN_DATA_OUT = "$FS_REQUEST_LOGIN_DATA_OUT";
	public static final String NEW_LOGIN_NAME = "$FS_NEW_LOGIN_NAME";


	public Login() {
		super(SystemRequest.Login);
	}


	@Override
	public void execute(IRequest request) throws QAntRequestValidationException {
		IQAntObject reqObj = request.getContent();
		String zoneName = reqObj.getUtfString(ZONE_NAME);
		String userName = reqObj.getUtfString("un");
		String password = reqObj.getUtfString("pw");

		IQAntObject params = (IQAntObject) request.getAttribute(REQUEST_LOGIN_DATA_OUT);
		if (params != null) {
			String newUserName = params.getUtfString(NEW_LOGIN_NAME);
			if (newUserName != null) {
				userName = newUserName;
			}
		}
		api.login(request.getSender(), userName, password, zoneName, params);
	}


	@Override
	public boolean validate(IRequest request) throws Exception {
		IQAntObject params = request.getContent();
		validateFormalParameters(params);

		String zoneName = params.getUtfString(ZONE_NAME);
		Zone zone = qant.getZoneManager().getZoneByName(zoneName);
		return customLogin(params, request, zone);
	}


	protected void validateFormalParameters(final IQAntObject qanto) throws QAntRequestValidationException {
		if (!qanto.containsKey("un") || !qanto.containsKey("pw") || !qanto.containsKey("zn")) {
			throw new QAntRequestValidationException(
					"Bad Login Request. Essential parameters are missing. Client API is probably fake.");
		}
	}


	protected boolean customLogin(IQAntObject params, IRequest request, Zone zone)
			throws QAntRequestValidationException {

		boolean res = true;
		if (zone != null && zone.isCustomLogin()) {
			if (zone.getExtension() == null) {
				throw new QAntRequestValidationException(
						"Custom login is ON but no Extension is active for this zone: " + zone.getName());
			}

			Map<IQAntEventParam, Object> sysParams = new HashMap<IQAntEventParam, Object>();
			sysParams.put(QAntEventSysParam.NEXT_COMMAND, Login.class);
			sysParams.put(QAntEventSysParam.REQUEST_OBJ, request);

			Map<IQAntEventParam, Object> userParams = new HashMap<IQAntEventParam, Object>();
			userParams.put(QAntEventParam.ZONE, zone);
			userParams.put(QAntEventParam.SESSION, request.getSender());
			userParams.put(QAntEventParam.LOGIN_NAME, params.getUtfString("un"));
			userParams.put(QAntEventParam.LOGIN_PASSWORD, params.getUtfString("pw"));
			userParams.put(QAntEventParam.LOGIN_IN_DATA, params.getQAntObject("p"));

			IQAntObject paramsOut = QAntObject.newInstance();
			request.setAttribute(REQUEST_LOGIN_DATA_OUT, paramsOut);
			userParams.put(QAntEventParam.LOGIN_OUT_DATA, paramsOut);

			qant.getEventManager().dispatchEvent(new QAntSystemEvent(QAntEventType.USER_LOGIN, userParams, sysParams));
			res = false;
		}
		return res;
	}


	@Override
	public Object preProcess(IRequest request) throws Exception {
		return null;
	}

}
