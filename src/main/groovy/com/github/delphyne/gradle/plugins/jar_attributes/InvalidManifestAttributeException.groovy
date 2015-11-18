package com.github.delphyne.gradle.plugins.jar_attributes

class InvalidManifestAttributeException extends RuntimeException {
	InvalidManifestAttributeException() {
		super()
	}

	InvalidManifestAttributeException(String message) {
		super(message)
	}

	InvalidManifestAttributeException(String message, Throwable cause) {
		super(message, cause)
	}

	InvalidManifestAttributeException(Throwable cause) {
		super(cause)
	}

	protected InvalidManifestAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace)
	}
}
