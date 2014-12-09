/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dozer.converters;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BigIntegerConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.CharacterConverter;
import org.apache.commons.beanutils.converters.ClassConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.apache.commons.lang3.ClassUtils;
import org.dozer.util.MappingUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal class for converting between wrapper types(including primitives). Only intended for internal use.
 * 
 * @author tierney.matt
 * @author garsombke.franz
 * @author benson.matt
 * @author dmitry.buzdin
 */
public class PrimitiveOrWrapperConverter {

  private static final Map<Class, Converter> CONVERTER_MAP = new HashMap<Class, Converter>();

  static {
    CONVERTER_MAP.put(Integer.class, new IntegerConverter());
    CONVERTER_MAP.put(Double.class, new DoubleConverter());
    CONVERTER_MAP.put(Short.class, new ShortConverter());
    CONVERTER_MAP.put(Character.class, new CharacterConverter());
    CONVERTER_MAP.put(Long.class, new LongConverter());
    CONVERTER_MAP.put(Boolean.class, new BooleanConverter());
    CONVERTER_MAP.put(Byte.class, new ByteConverter());
    CONVERTER_MAP.put(Float.class, new FloatConverter());
    CONVERTER_MAP.put(BigDecimal.class, new BigDecimalConverter());
    CONVERTER_MAP.put(BigInteger.class, new BigIntegerConverter());
    CONVERTER_MAP.put(Class.class, new ClassConverter());
  }

  public Object convert(Object srcFieldValue, Class destFieldClass, DateFormatContainer dateFormatContainer) {
    if (srcFieldValue == null || destFieldClass == null || (srcFieldValue.equals("") && !destFieldClass.equals(String.class))) {
      return null;
    }
    Converter converter = getPrimitiveOrWrapperConverter(destFieldClass, dateFormatContainer);
    try {
      return converter.convert(destFieldClass, srcFieldValue);
    } catch (org.apache.commons.beanutils.ConversionException e) {
      throw new org.dozer.converters.ConversionException(e);
    }
  }

  private Converter getPrimitiveOrWrapperConverter(Class destClass, DateFormatContainer dateFormatContainer) {
    if (String.class.equals(destClass)) {
      return new StringConverter(dateFormatContainer);
    }

    Converter result = CONVERTER_MAP.get(ClassUtils.primitiveToWrapper(destClass));

    if (result == null) {
      if (java.util.Date.class.isAssignableFrom(destClass)) {
        result = new DateConverter(dateFormatContainer.getDateFormat());
      } else if (Calendar.class.isAssignableFrom(destClass) ) {
        result = new CalendarConverter(dateFormatContainer.getDateFormat());
      } else if (XMLGregorianCalendar.class.isAssignableFrom(destClass)){
        result = new XMLGregorianCalendarConverter(dateFormatContainer.getDateFormat());
      } else if (MappingUtils.isEnumType(destClass)){
        result = new EnumConverter();
      }
    }
    return result == null ? new StringConstructorConverter(dateFormatContainer) : result;
  }

  public boolean accepts(Class<?> aClass) {
    return aClass.isPrimitive()
            || Number.class.isAssignableFrom(aClass)
            || String.class.equals(aClass)
            || Character.class.equals(aClass)
            || Boolean.class.equals(aClass)
            || java.util.Date.class.isAssignableFrom(aClass)
            || java.util.Calendar.class.isAssignableFrom(aClass);
  }

}