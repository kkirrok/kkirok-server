package com.kkirok.server.global.external;

public interface ExternalClient<P, R> {
	R execute(P request);
}
