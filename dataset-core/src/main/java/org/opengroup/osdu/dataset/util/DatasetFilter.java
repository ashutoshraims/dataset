// Copyright © 2021 Amazon Web Services
// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.dataset.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.http.ResponseHeadersFactory;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatasetFilter implements Filter {

	private static final String DISABLE_AUTH_PROPERTY = "org.opengroup.osdu.dataset.disableAuth";
	private static final String OPTIONS_STRING = "OPTIONS";
	private static final String FOR_HEADER_NAME = "frame-of-reference";


	@Inject
	private DpsHeaders dpsHeaders;

	private ResponseHeadersFactory responseHeadersFactory = new ResponseHeadersFactory();

	// defaults to * for any front-end, string must be comma-delimited if more than one domain
	@Value("${ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS:*}")
	String ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		boolean disableAuth = Boolean.getBoolean(DISABLE_AUTH_PROPERTY);
		if (disableAuth) {
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String fetchConversionHeader = ((HttpServletRequest) request).getHeader(FOR_HEADER_NAME);
		if (!Strings.isNullOrEmpty(fetchConversionHeader)) {
			this.dpsHeaders.put(FOR_HEADER_NAME, fetchConversionHeader);
		}

		HttpServletResponse httpResponse = (HttpServletResponse) response;

		this.dpsHeaders.addCorrelationIdIfMissing();

		Map<String, String> responseHeaders = responseHeadersFactory.getResponseHeaders(ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS);
		for(Map.Entry<String, String> header : responseHeaders.entrySet()){
			httpResponse.setHeader(header.getKey(), header.getValue());
		}

		// This block is needed to handle CWE-601
		String correlationId = this.dpsHeaders.getCorrelationId();
		Pattern redirectPattern = Pattern.compile("redirect");
		boolean findRedirect = redirectPattern.matcher(correlationId).find();
		if (!findRedirect) {
			httpResponse.addHeader(DpsHeaders.CORRELATION_ID, correlationId);
		} else {
			throw new ServletException("Correlation Id contains redirecting String");
		}
		// This block handles the OPTIONS preflight requests performed by Swagger. We
		// are also enforcing requests coming from other origins to be rejected.
		if (httpRequest.getMethod().equalsIgnoreCase(OPTIONS_STRING)) {
			httpResponse.setStatus(HttpStatus.SC_OK);
		}

		chain.doFilter(httpRequest, httpResponse);
	}

	@Override
	public void destroy() {
	}
}
