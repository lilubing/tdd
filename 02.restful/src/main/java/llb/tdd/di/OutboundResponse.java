package llb.tdd.di;

import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

import java.lang.annotation.Annotation;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: OutboundResponse
 * @date 2022-11-09 7:35:01
 * @ProjectName tdd
 * @Version V1.0
 */
abstract class OutboundResponse extends Response {
	abstract GenericEntity getGenericEntity();

	abstract Annotation[] getAnnotations();
}
