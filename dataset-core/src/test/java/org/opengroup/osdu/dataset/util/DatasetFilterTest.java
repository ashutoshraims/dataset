package org.opengroup.osdu.dataset.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.UUID;


import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatasetFilterTest {

    @Mock
    private DpsHeaders dpsHeaders;

    @InjectMocks
    private DatasetFilter datasetFilter;

    private static final String FOR_HEADER_NAME = "frame-of-reference";

    @Test
    public void doFilter()
            throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(FOR_HEADER_NAME,"dummy-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        when(dpsHeaders.getCorrelationId()).thenReturn(UUID.randomUUID().toString());
        datasetFilter.doFilter(request,response,filterChain);
        verify(dpsHeaders,times(1)).getCorrelationId();
        verify(dpsHeaders,times(1)).addCorrelationIdIfMissing();
    }

    @Test (expected = ServletException.class)
    public void doFilterInvalidCorrelationId()
            throws IOException, ServletException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(FOR_HEADER_NAME,"dummy-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        when(dpsHeaders.getCorrelationId()).thenReturn("redirectToAPhishingURL");
        datasetFilter.doFilter(request,response,filterChain);
        verify(dpsHeaders,times(1)).getCorrelationId();
        verify(dpsHeaders,times(1)).addCorrelationIdIfMissing();
    }
}
