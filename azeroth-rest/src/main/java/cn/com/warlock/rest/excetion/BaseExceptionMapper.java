package cn.com.warlock.rest.excetion;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.warlock.rest.response.ResponseCode;
import cn.com.warlock.rest.response.WrapperResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * REST 异常映射
 * 
 * 将异常转换成RestResponse
 * 
 *
 */
public class BaseExceptionMapper implements ExceptionMapper<Exception> {

    private static Logger      log = LoggerFactory.getLogger(BaseExceptionMapper.class);

    @Context
    private HttpServletRequest request;

    private ExcetionWrapper    excetionWrapper;

    public BaseExceptionMapper() {
    }

    public BaseExceptionMapper(ExcetionWrapper excetionWrapper) {
        super();
        this.excetionWrapper = excetionWrapper;
    }

    @Override
    public Response toResponse(Exception e) {

        WrapperResponseEntity response = null;
        if (e instanceof NotFoundException) {
            response = new WrapperResponseEntity(ResponseCode.NOT_FOUND);
        } else if (e instanceof NotAllowedException) {
            response = new WrapperResponseEntity(ResponseCode.FORBIDDEN);
        } else if (e instanceof JsonProcessingException) {
            response = new WrapperResponseEntity(ResponseCode.ERROR_JSON);
        } else if (e instanceof NotSupportedException) {
            response = new WrapperResponseEntity(ResponseCode.UNSUPPORTED_MEDIA_TYPE);
        } else {
            response = excetionWrapper != null ? excetionWrapper.toResponse(e) : null;
            if (response == null)
                response = new WrapperResponseEntity(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return Response.status(response.httpStatus()).type(MediaType.APPLICATION_JSON)
            .entity(response).build();
    }

}
