package io.dnrdl12.remittance.comm.utills;

import java.util.Collection;
import java.util.Map;

/**
 * packageName    : com.unbiz.gms.comm.utils
 * fileName       : StringUtils
 * author         : UNBIZ
 * date           : 2022-09-16
 * description    : StringUtils
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022-09-16        UNBIZ              최초 생성
 */
public class StringUtils
{
    /** Null 문자 */
    private static final String NULLSTR = "";

    /** 구분자 */
    private static final char SEPARATOR = '_';

    /**
     * value가 null이면 defaultValue를 리턴
     *
     * @param value
     * @return value
     */
    public static <T> T nvl(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * Collection(List，Set，Queue)가 비어 있는지 체크
     *
     * @param coll Collection
     * @return true/ false
     */
    public static boolean isEmpty(Collection<?> coll)
    {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * Collection(List，Set，Queue)가 비어 있지 않는지 체크
     *
     * @param coll Collection
     * @return true/ false
     */
    public static boolean isNotEmpty(Collection<?> coll)
    {
        return !isEmpty(coll);
    }

    /**
     * Object[]이 비어 있는지 체크
     *
     * @param objects
     ** @return true/ false
     */
    public static boolean isEmpty(Object[] objects)
    {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * Object[]이 비어 있지 않는지 체크
     *
     * @param objects
     * @return true/ false
     */
    public static boolean isNotEmpty(Object[] objects)
    {
        return !isEmpty(objects);
    }

    /**
     * Map이 비어 있는지 체크
     *
     * @param map
     * @return true/ false
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return isNull(map) || map.isEmpty();
    }

    /**
     * Map이 비어 있지 않는지 체크
     *
     * @param map
     * @return true/ false
     */
    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }

    /**
     * String이 비어 있는지 체크
     *
     * @param str String
     * @return true/ false
     */
    public static boolean isEmpty(String str)
    {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * String이 비어 있지 않는지 체크
     *
     * @param str String
     * @return true/ false
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    /**
     * Object가 비어 있는지 체크
     * @param obj
     * @return true/ false
     */
    public static boolean isEmpty(Object obj)
    {
        return isNull(obj) || NULLSTR.equals(toString(obj).trim());
    }

    /**
     * Object가 비어 있지 않는지 체크
     * @param obj
     * @return true/ false
     */
    public static boolean isNotEmpty(Object obj)
    {
        return !isEmpty(obj);
    }

    /**
     * Object 가 null인지 체크
     *
     * @param object Object
     * @return true/ false
     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * Object 가 null이 아닌지 체크
     *
     * @param object Object
     * @return true/ false
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    /**
     * Object 가 Array인지 체크
     *
     * @param object
     * @return true/ false
     */
    public static boolean isArray(Object object)
    {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * trim
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }

    /**
     * 문자열 자르기
     *
     * @param str
     * @param start
     * @return
     */
    public static String substring(final String str, int start)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = str.length() + start;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (start > str.length())
        {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * 문자열 자르기
     *
     * @param str
     * @param start
     * @param end
     * @return
     */
    public static String substring(final String str, int start, int end)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (end < 0)
        {
            end = str.length() + end;
        }
        if (start < 0)
        {
            start = str.length() + start;
        }

        if (end > str.length())
        {
            end = str.length();
        }

        if (start > end)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 下划线转驼峰命名
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param str 验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name)
    {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty())
        {
            // 没必要转换
            return "";
        }
        else if (!name.contains("_"))
        {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels)
        {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty())
            {
                continue;
            }
            // 首字母大写
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 카멜케이스 변경 ：user_name->userName
     */
    public static String toCamelCase(String s)
    {
        if (s == null)
        {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (c == SEPARATOR)
            {
                upperCase = true;
            }
            else if (upperCase)
            {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 공백이나 null이 아닐 경우 문자열 리턴, 그외에는 공백 리턴
     * @param val Object
     * @return String
     */
    public static String toString(Object val){
        String result = "";
        if(val != null && isNotEmpty(val.toString())){
            result =String.valueOf(val);
        }
        return result;
    }

    /**
     * 두 Object(String)의 문자열을 비교하여 일치여부를 리턴한다.
     * @param val1 비교 문자열 1
     * @param val2 비교 문자열 2
     * @return true(일치), true(미 일치)
     */
    public static boolean isEquals(Object val1, Object val2){
        if(isEmpty(val1)) return false;
        if(isEmpty(val2)) return false;
        if(toString(val1).equals(toString(val2))) return true;

        return false;
    }

    /**
     * 두 Object(String)의 문자열을 비교하여 일치 하지 않음을 리턴한다.
     * @param val1 비교 문자열 1
     * @param val2 비교 문자열 2
     * @return true(일치), true(미 일치)
     */
    public static boolean isNotEquals(Object val1, Object val2){
        if(isEmpty(val1)) return true;
        if(isEmpty(val2)) return true;
        if(toString(val1).equals(toString(val2))) return false;

        return true;
    }

    /**
     * 하이픈(-)이 없는 형태의 날짜 문자열을 하이픈(-)형태의 문자열로 변경한다.
     * ( YYYYMM -> YYYY-MM, YYYYMMDD -> YYYY-MM-DD)
     * 예시 : dateFormatting("202105")   -> 2021-05
     *       dateFormatting("20210526") -> 2021-05-26
     * @param val 문자열
     * @return result
     */
    public static String dateFormatting(String val){
        String result = "";
        if(isNull(val)) {
            return null;
        }
        if(val.length() == 6){
            result = val.substring(0,4) + "-" + val.substring(4);
        }else if(val.length() == 8){
            result = val.substring(0,4) + "-" + val.substring(4,6) + "-" + val.substring(6);
        }
        return result;
    }

}