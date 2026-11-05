//package com.knot.provisioning.utill;
//
//import javax.net.ssl.SSLContext;
//
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
//import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
//import org.apache.hc.core5.ssl.SSLContextBuilder;
//import org.apache.hc.core5.ssl.TrustStrategy;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//
//public class SslCertificate {
//
//	public static RestTemplate restTemplate() throws Exception {
//		TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
//
//		SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, acceptingTrustStrategy).build();
//
//		@SuppressWarnings("deprecation")
//		var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
//				.setSSLSocketFactory(org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder.create()
//						.setSslContext(sslContext).setHostnameVerifier(NoopHostnameVerifier.INSTANCE).build())
//				.build();
//
//		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
//
//		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//
//		return new RestTemplate(requestFactory);
//
//	}
//}
//
