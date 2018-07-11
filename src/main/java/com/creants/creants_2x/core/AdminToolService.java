package com.creants.creants_2x.core;

import com.creants.creants_2x.QAntServer;
import com.creants.creants_2x.core.entities.QAntZone;
import com.creants.creants_2x.core.entities.Zone;
import com.creants.creants_2x.core.extension.ExtensionLevel;
import com.creants.creants_2x.core.extension.ExtensionReloadMode;
import com.creants.creants_2x.core.extension.ExtensionType;
import com.creants.creants_2x.core.extension.IQAntExtension;
import com.creants.creants_2x.core.extension.QAntExtension;
import com.creants.creants_2x.core.managers.IExtensionManager;
import com.creants.creants_2x.core.managers.IZoneManager;
import com.creants.creants_2x.core.service.IService;
import com.creants.creants_2x.core.util.QAntTracer;

/**
 * @author LamHM
 *
 */
public class AdminToolService implements IService {
	public static final String ZONE_NAME = "--=={{{ AdminZone }}}==--";
	private static final String EXT_NAME = "Admin";
	private static final String EXT_CLASS = "com.creants.v2.admin.AdminExtension";
	private QAntServer qantServer;
	private IExtensionManager extensionManager;
	private IZoneManager zoneManager;
	private IQAntExtension adminExtension;
	private Zone adminZone;
	private volatile boolean inited;


	public AdminToolService() {
		this.inited = false;
	}


	@Override
	public void init(Object obj) {
		if (inited) {
			throw new IllegalStateException("AdminToolService was already initialized!");
		}
		qantServer = QAntServer.getInstance();
		extensionManager = qantServer.getExtensionManager();
		zoneManager = qantServer.getZoneManager();
		initializeAdminZone();
		QAntTracer.info(this.getClass(), "AdminTool Service started");
	}


	private void initializeAdminZone() {
		(adminZone = new QAntZone("--=={{{ AdminZone }}}==--")).setId(-1);
		adminZone.setActive(true);
		adminZone.setCustomLogin(true);
		adminZone.setForceLogout(true);
		adminZone.setGuestUserAllowed(false);
		adminZone.setMaxAllowedRooms(1);
		adminZone.setMaxAllowedUsers(1000);
		adminZone.setMaxUserIdleTime(999999999);
		try {
			final Class<?> extClass = Class.forName(EXT_CLASS);
			(adminExtension = (QAntExtension) extClass.newInstance()).setActive(true);
			adminExtension.setLevel(ExtensionLevel.ZONE);
			adminExtension.setName(EXT_NAME);
			adminExtension.setParentZone(this.adminZone);
			adminExtension.setReloadMode(ExtensionReloadMode.NONE);
			adminExtension.setType(ExtensionType.JAVA);
		} catch (Exception e) {
			QAntTracer.error(this.getClass(), QAntTracer.getTraceMessage(e));
		}
		adminZone.setExtension(this.adminExtension);
		extensionManager.addExtension(this.adminExtension);
		zoneManager.addZone(adminZone);
		adminExtension.init();
	}


	@Override
	public void destroy(Object obj) {
		adminExtension.setActive(false);
		adminZone.setActive(false);
		adminExtension = null;
		adminZone = null;
	}


	@Override
	public void handleMessage(Object message) throws Exception {
		throw new UnsupportedOperationException("This operation is not supported.");
	}


	@Override
	public String getName() {
		return "AdminToolService";
	}


	@Override
	public void setName(String name) {
	}

}
