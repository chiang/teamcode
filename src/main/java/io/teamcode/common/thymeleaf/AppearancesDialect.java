package io.teamcode.common.thymeleaf;

import io.teamcode.common.AppearancesHelper;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 25..
 */
public class AppearancesDialect extends AbstractDialect implements IExpressionEnhancingDialect {

    public AppearancesDialect() {
        super();
    }

    @Override
    public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
        Map<String, Object> expressions = new HashMap<>();
        expressions.put("appearances", new AppearancesHelper());

        return expressions;
    }

    /**
     * @see org.thymeleaf.dialect.IDialect#getPrefix
     *
     * @return
     */
    @Override
    public String getPrefix() {
        //return "xxx";

        return null;
    }
}
