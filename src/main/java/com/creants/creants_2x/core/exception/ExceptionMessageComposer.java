package com.creants.creants_2x.core.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LamHM
 *
 */
public class ExceptionMessageComposer {
	private static final String NEW_LINE;
	public static volatile boolean globalPrintStackTrace;
	public static volatile boolean useExtendedMessages;
	private String mainErrorMessage;
	private String exceptionType;
	private String description;
	private String possibleCauses;
	private String stackTrace;
	private List<String> additionalInfos;
	private StringBuilder buf;
	static {
		NEW_LINE = System.getProperty("line.separator");
		ExceptionMessageComposer.globalPrintStackTrace = true;
		ExceptionMessageComposer.useExtendedMessages = true;
	}


	public ExceptionMessageComposer(Throwable t) {
		this(t, ExceptionMessageComposer.globalPrintStackTrace);
	}


	public ExceptionMessageComposer(Throwable t, boolean printStackTrace) {
		mainErrorMessage = t.getMessage();
		if (mainErrorMessage == null) {
			mainErrorMessage = "*** Null ***";
		}
		exceptionType = t.getClass().getName();
		buf = new StringBuilder();
		if (printStackTrace) {
			setStackTrace(t);
		}
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public void setPossibleCauses(String possibleCauses) {
		this.possibleCauses = possibleCauses;
	}


	private void setStackTrace(Throwable t) {
		// this.stackTrace = Logging.formatStackTrace(t.getStackTrace());
	}


	public void addInfo(String infoMessage) {
		if (additionalInfos == null) {
			additionalInfos = new ArrayList<String>();
		}
		additionalInfos.add(infoMessage);
	}


	@Override
	public String toString() {
		if (!ExceptionMessageComposer.useExtendedMessages) {
			buf.append(exceptionType).append(": ").append(mainErrorMessage);
			return buf.toString();
		}

		buf.append(this.exceptionType).append(":").append(ExceptionMessageComposer.NEW_LINE);
		buf.append("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
				.append(ExceptionMessageComposer.NEW_LINE);
		buf.append("Exception: ").append(this.exceptionType).append(ExceptionMessageComposer.NEW_LINE);
		buf.append("Message: ").append(this.mainErrorMessage).append(ExceptionMessageComposer.NEW_LINE);
		if (description != null) {
			buf.append("Description: ").append(description).append(ExceptionMessageComposer.NEW_LINE);
		}
		if (possibleCauses != null) {
			buf.append("Possible Causes: ").append(possibleCauses).append(ExceptionMessageComposer.NEW_LINE);
		}

		if (additionalInfos != null) {
			additionalInfos.forEach(info -> buf.append(info).append(ExceptionMessageComposer.NEW_LINE));
		}

		if (stackTrace != null) {
			buf.append("+--- --- ---+").append(ExceptionMessageComposer.NEW_LINE);
			buf.append("Stack Trace:").append(ExceptionMessageComposer.NEW_LINE);
			buf.append("+--- --- ---+").append(ExceptionMessageComposer.NEW_LINE);
			buf.append(stackTrace);
			buf.append("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
					.append(ExceptionMessageComposer.NEW_LINE);
		} else {
			buf.append("::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
					.append(ExceptionMessageComposer.NEW_LINE);
		}
		return buf.toString();
	}
}
