package com.scrippsnetworks.wcm.url;
 
import java.lang.String;
import org.apache.sling.api.resource.ResourceResolver;
import javax.servlet.http.HttpServletRequest;


/** 
 * <p>
 * UrlMapper uses the same map/resolve paradigm as the Sling ResourceResolver in order to
 * remove/add the "bucketing" hierarchy from paths. Unlike the ResourceResolver, though,
 * the resolve methods return string paths instead of Resources, so are named resolvePath.
 * Also, the resolvePath methods return null if they have no corrections to make to the URL path.
 * That doesn't mean the URL path won't resolve to a resource (in fact, it probably does), just that
 * it had no bucketing to be applied/reapplied.
 * </p>
 *
 * <p>
 * "Bucketing" refers to a method used to work around a limitation in the JCR.
 * In order to avoid JCR nodes with large numbers of children, artificial levels of hierarchy are
 * used. For example, a recipe may wind up in <code>/content/&lt;site&gt;/recipes/&lt;chef-name&gt;/r/re/rec/reci/recipe-name</code>,
 * with the "bucketing" consisting of the path segment <code>/r/re/rec/reci</code>.
 * </p>
 *
 * <p>
 * Mapping a path consists in creating a new path (for use in URLs for example) with these levels of "bucketing" removed.
 * also taking into account the resource resolver's mapping rules to do prefix removal. For example,
 * <code>/content/cook/recipes/a-chef/r/re/rec/reci/recipe-name</code> could map to <code>/recipes/a-chef/recipe-name</code>
 * after removing the bucketing layers and stripping the <code>/content/cook</code> prefix using the resource resolver's map.
 * </p>
 *
 * <p>
 * Resolving a path applies the reverse logic, reapplying bucketing.  The map and resolve implementations are agnostic about
 * the existence of the resources indicated by the paths, with the exception that the indicated section path (e.g.,
 * /content/cook/&lt;section-name&gt;) must exist The map methods also use the ResourceResolver methods, but they simply do
 * path segment removal and <em>never</em> try to resolve the path given to them.
 * </p>
 *
 *
 * @author Scott Johnson @see ResourceResolver
 */
public interface UrlMapper {

    /** Maps an absolute resource path to a shortened URL path with "bucketing" removed.
     *
     * <p>
     * The mapping takes into account the resource resolver's mapping rules.
     * </p>
     *
     * @param resourceResolver Resource resolver to use for mapping.
     * @param request Servlet request to use in mapping.
     * @param path Absolute path or URL with absolute path. The mapper is agnostic
     *      about whether a hostname is ours or not, but obviously the result won't be
     *      useful if it is not (and thus the path isn't for a resource).
     * @return String shortened URL path
     */
    public String map(ResourceResolver resourceResolver, HttpServletRequest request, String path);

    /** Maps an absolute resource path to a shortened URL path with "bucketing" removed.
     *
     * <p>
     * The mapping takes into account the resource resolver's mapping rules. Using this form
     * the resource resolver is taken from the servlet request if possible, otherwise
     * and administrative resource resolver is used. The form taking a ResourceResolver
     * parameter should be preferred.
     * </p>
     *
     * @param request servlet request to use in mapping
     * @param path Absolute path or URL with absolute path. The mapper is agnostic
     *      about whether a hostname is ours or not, but obviously the result won't be
     *      useful if it is not (and thus the path isn't for a resource).
     * @return String shortened URL path
     */
    public String map(HttpServletRequest request, String path);

    /** Maps an absolute resource path to a shortened URL path with no bucketing.
     *
     * <p>
     * Since no request is provided with which to get information from the resource resolver's map,
     * on how to shorten the path prefix (e.g., /content/cook/section-name to /section-name) the path
     * should already have any mapped prefix removed (e.g., /&lt;section-name&gt; instead of /content/cook/&lt;section-name&gt;).
     * </p>
     *
     * @param path absolute resource path to shorten to url path
     * @return String shortened URL path
     */
    public String map(String path);

    /* Resolves a shortened URL path to a full resource path with "bucketing".
     *
     * <p>
     * The resolution takes into account the resource resolver's mapping rules.
     * </p>
     *
     * @param resourceResolver a resource resolver to use in finding the resource
     * @param request Servlet request to use in resolving real root path. The sling API docs say that null may be provided,
     *      in which case "the implementation should use reasonable defaults". You probably won't find  the default reasonable.
     * @param path The "absolute" URL path (starting with /) to resolve.
     * @return String full path to resource
     */
    // public String resolve(ResourceResolver resourceResolver, HttpServletRequest request, String path);

    /** Resolves a shortened URL path to a full resource path with "bucketing".
     *
     * <p>
     * The resolution takes into account the resource resolver's mapping rules.
     * This method is meant to be used for internal forwarding logic, only returning a path
     * if SNI-specific bucketing logic had to be applied.
     * </p>
     *
     * @param resourceResolver The resource resolver to use. If the resolver is null, the return value is null.
     * @param request Servlet request to use in resolving real root path. The sling API docs say that null may be provided,
     *      in which case "the implementation should use reasonable defaults". You probably won't find  the default reasonable.
     * @param path The "absolute" URL path (starting with /, but not necessarily the content root) to resolve. Paths
     *  not starting with a slash will have the leading slash prepended.
     * @return String An absolute path to a content page resource, or null if the path is resolveable as-is by the
     *  ResourceResolver.
     */
    public String resolvePath(ResourceResolver resourceResolver, HttpServletRequest request, String path);

    /* Resolves a shortened URL path to a full resource path with "bucketing".
     *
     * <p>
     * The resolution takes into account the resource resolver's mapping rules.
     * Using this form, a resource resolver, if needed, would be requested from the ResourceResolverFactory.
     * </p>
     *
     * @param request Servlet request to use in resolving real root path. The sling API docs say that null may be provided,
     *      in which case "the implementation should use reasonable defaults". You probably won't find  the default reasonable.
     * @param path The "absolute" URL path (starting with /) to resolve.
     * @return String full path to resource
     */
    // public String resolve(HttpServletRequest request, String path);

    /** Resolves a shortened URL path to a full resource path with "bucketing".
     *
     * <p>
     * The resolution takes into account the resource resolver's mapping rules.
     * This method is meant to be used for internal forwarding logic, only returning a path
     * if SNI-specific bucketing logic had to be applied.
     * </p>
     *
     * @param request Servlet request to use in resolving real root path. The sling API docs say that null may be provided,
     *      in which case "the implementation should use reasonable defaults". You probably won't find  the default reasonable.
     * @param path The "absolute" URL path (starting with /, but not necessarily the content root) to resolve. Paths
     *  not starting with a slash will have the leading slash prepended.
     * @return String An absolute path to a content page resource, or null if the path is resolveable as-is by the
     *  ResourceResolver.
     */
    public String resolvePath(HttpServletRequest request, String path);

    /* Resolves an absolute url path (beginning at the root) into resource path with "bucketing".
     *
     * <p>
     * Like the resource resolver's method, the path is expected to be absolute, starting at the root.
     * If the path is absolute, null, or empty, the root path '/' is prepended to the path to make it
     * absolute.
     * </p>
     *
     * <p>
     * If you're using this method, you should supply the real root path starting with /content/&lt;site&gt;. If you want
     * the resource resolver to figure out the root, use one of the methods taking a request as a parameter, and the request
     * will be used to figure out the root path using the resource resolver's map.
     * </p>
     *
     * @param path a path beginning at the root
     * @return String a resource path
     */
    // public String resolve(String path);

}
