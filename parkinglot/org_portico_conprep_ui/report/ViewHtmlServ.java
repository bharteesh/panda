package org.portico.conprep.ui.report;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.portico.common.characterconversion.CharacterConversionService;
import org.portico.common.characterconversion.CharacterConversionServiceFactory;
import org.portico.common.characterconversion.CharacterConversionUtil;
import org.portico.common.config.Config;
import org.portico.common.config.LdapUtil;
import org.portico.common.context.SessionContext;
import org.portico.common.persistence.ObjectFactory;
import org.portico.common.toolregistry.ToolRegistryService;
import org.portico.common.toolservice.ToolBroker;
import org.portico.common.toolservice.ToolReply;
import org.portico.common.toolservice.ToolReplyHandler;
import org.portico.common.toolservice.xml.toolresult.ToolResult;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.workflow.common.WorkflowToolUtil;
import org.portico.conprep.workflow.content.Batch;
import org.portico.conprep.workflow.content.BatchOperationsManager;
import org.portico.conprep.workflow.content.SuState;
import org.portico.conprep.workflow.impl.documentum.ActionTool;
import org.portico.conprep.workflow.render.RenderGroup;
import org.portico.conprep.workflow.render.Renderer;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.HttpSessionState;
import com.documentum.web.contentxfer.Trace;
import com.documentum.web.formext.session.SessionManagerHttpBinding;

public class ViewHtmlServ extends HttpServlet implements ToolReplyHandler {
	// private final static boolean TOOL_VERSION = true;
	static CharacterConversionService ccs=null;
    public final static String WORKAREA_PATH = "workarea";
    public final static String PORTICO_HOME = "PORTICO_HOME"; // env variable for Portico home directory

	public void init()throws ServletException{
		super.init();
		try{
			CharacterConversionServiceFactory ccsf = CharacterConversionUtil.getFactory();
			ccs = ccsf.createService();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public ViewHtmlServ() {
	}

	public final String getServletInfo() {
		return getServletConfig().getServletName();
	}

	public final void service(HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws ServletException,
			IOException {
		if (Trace.GETCONTENT)
			log("GetContentServlet: " + getServletInfo() + " servlet initiated");
		if (Trace.GETCONTENT) {
			// StringBuffer stringbuffer =
			// HttpUtils.getRequestURL(httpservletrequest);
			StringBuffer stringbuffer = httpservletrequest.getRequestURL();
			String s = httpservletrequest.getQueryString();
			if (s != null && s.length() > 0)
				stringbuffer.append("?").append(s);
			log("GetContentServlet: Request = " + stringbuffer.toString());
		}
		try {
			super.service(httpservletrequest, httpservletresponse);
		} catch (RuntimeException runtimeexception) {
			log("GetContentServlet: Exception: " + runtimeexception.toString());
			throw runtimeexception;
		} finally {
			if (Trace.GETCONTENT)
				log("GetContentServlet: " + getServletInfo()
						+ " servlet completed");
		}
	}

	public final void doGet(HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws ServletException,
			IOException {
		getContent(httpservletrequest, httpservletresponse);
	}

	public final void doPost(HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws ServletException,
			IOException {
		getContent(httpservletrequest, httpservletresponse);
	}

	public final void log(String s) {
		com.documentum.web.common.Trace.println(s);
		super.log(s);
	}

	private byte[] fetchLeadMDFile(String mObjId) throws DfException, Exception {
		byte[] bytes = null;
		try {
			IDfDocument mesgObj = (IDfDocument) m_dfSession.getObject(new DfId(
					mObjId));
			if (mesgObj.getContentSize() > 0) {
				ByteArrayInputStream in = mesgObj.getContent();
				int count = in.available();
				if (count > 0) {
					bytes = new byte[count];
					in.read(bytes, 0, count);
				}

			}
			return bytes;
		} catch (DfException e) {
			throw e;
		}
	}

	public boolean processToolReply(ToolReply reply, String filename,
			Map arguments) throws Exception {
		HelperClass.porticoOutput(0, "ViewHtmlServ, Processing tool reply");
		boolean successFlag = true;
		HelperClass.porticoOutput(0, "ViewHtmlServ, before getResultCode");
		int resultCode = reply.getResultCode();
		HelperClass.porticoOutput(0,
				"ViewHtmlServ, after getResultCode, before getToolResult");
		ToolResult result = WorkflowToolUtil.getToolResult(reply);
		HelperClass.porticoOutput(0, "ViewHtmlServ, after getToolResult");

		if (resultCode != ToolReply.SUCCESS) {
			successFlag = false;
			HelperClass.porticoOutput(0, "ViewHtmlServ, Tool failed");
		}
		return successFlag;
	}

	private void getContent(HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse) throws ServletException,
			IOException, IllegalArgumentException {
		String cuId = httpservletrequest.getParameter("objectId");
		if (cuId == null || cuId.length() == 0)
			throw new IllegalArgumentException(
					"Parameter objectId is mandatory");
		String batchId = httpservletrequest.getParameter("batchId");
       //if(batchId.startsWith("ark:/")) {
       // String batchId1 = "";
       // batchId1=DBBatchObject.getBatchIdFromBatchAccessionId(batchId);
       // batchId=batchId1;
       //}
		if (batchId == null || batchId.length() == 0)
			throw new IllegalArgumentException("Parameter batchId is mandatory");
		String rendName = httpservletrequest.getParameter("rendName");
		if (rendName == null || rendName.length() == 0)
			throw new IllegalArgumentException(
					"Parameter rendName is mandatory");

		HttpSessionState.bindHttpSession(httpservletrequest.getSession());
		ActionTool at = null;
		try {
			IDfSessionManager idfsessionmanager = SessionManagerHttpBinding
					.getSessionManager();
			IDfSession idfsession = idfsessionmanager
					.getSession(SessionManagerHttpBinding.getCurrentDocbase());
			httpservletresponse.setContentType("application/xhtml+xml");

			String uriPrefix = "wdk5-download?objectId=";
			at = new ActionTool(idfsession, batchId);
			at.flush();
			m_dfSession = idfsession;
			if (at.getIsRenderable(cuId)) {
				RenderGroup rg = at.getRenderGroup(cuId);
				HelperClass.porticoOutput(0, "invoking rg.getRenderSource()");
				String suId = rg.getRenderSource();
				String htmlrendering = LdapUtil.getAttribute("dc=ui",
						"cn=conprepui", "htmlrendering");
				HelperClass.porticoOutput(0, "htmlrendering=" + htmlrendering);
				if (htmlrendering.equals("tool")) {
                   
					SessionContext sessionContext = SessionContext.getInstance();
					ObjectFactory factory = sessionContext.getObjectFactory();
					Batch batch = (Batch)factory.load(Batch.class,batchId);
					BatchOperationsManager batchOperationsManager = batch.getBatchOperationsManager();
					String inputRelPath = batchOperationsManager.getWorkareaInputPath();

                    String rootPath = Config.getInstance().getPorticoHome() + "/" + WORKAREA_PATH + "/";
                    SuState suState = (SuState)factory.load(SuState.class,suId);
					String suFilename = WorkflowToolUtil.exposeBaseDocument(suState);
					String outputRelPath = batchOperationsManager.getWorkareaOutputPath();
					int i2 = suFilename.lastIndexOf("/");
					String parm2 = suFilename.substring(i2 + 1);
					String thread = String.valueOf(Thread.currentThread()
							.getId());
					String machineName = InetAddress.getLocalHost()
							.getCanonicalHostName();
					String time = String.valueOf(System.currentTimeMillis());
					String suFilename1 = outputRelPath + "/"
							+ "Html_Render_1.0" + "/" + parm2 + "/" + parm2
							+ "-" + thread + "-" + time + "-" + machineName
							+ ".html";
					HelperClass.porticoOutput(0, "ipRP=" + inputRelPath
							+ " rtP=" + rootPath + "suFnm=" + suFilename);
					HelperClass.porticoOutput(0, "opRP=" + outputRelPath
							+ " rtP=" + rootPath + "suFnm1=" + suFilename1);
					Map argMap = new HashMap();
					argMap.put("uriPrefix", uriPrefix);
					argMap.put("rendName", rendName);
					argMap.put("rg", (RenderGroup) rg);
					argMap.put(ToolRegistryService.ARG_SUPPORTING_FORMAT_ID, suState
							.getFormatId());
					HelperClass.porticoOutput(0, "start tool invoke");
					ToolBroker broker = new ToolBroker();
					broker.process(suFilename, suFilename1,
							ToolRegistryService.SVC_HTML_RENDITION, argMap,
							this);

					String outfname = rootPath + suFilename1;
					FileInputStream in = new FileInputStream(outfname);
					byte[] html = null;
					int count = in.available();
					if (count > 0) {
						html = new byte[count];
						in.read(html, 0, count);
					}
					HelperClass.porticoOutput(0, "return from tool invoke");
					if (html.length < 1)
						HelperClass.porticoOutput(0,
								"transformed file length=0");
					else {
						if( null == ccs){
							CharacterConversionServiceFactory ccsf = CharacterConversionUtil
								.getFactory();
							ccs = ccsf.createService();
						}
						byte[] html2 = ccs.convert(html);
						javax.servlet.ServletOutputStream servletoutputstream = httpservletresponse
								.getOutputStream();
						servletoutputstream.write(html2);
						servletoutputstream.flush();
					}

				} else {
					Renderer r1 = Renderer.getRendererInstance(rendName);
					byte[] xml = fetchLeadMDFile(suId);
					if (xml.length > 0) {
						HelperClass.porticoOutput(0, "start render invoke");
						byte[] html1 = r1.render(xml, rg, uriPrefix);
						HelperClass.porticoOutput(0,
								"return from render invoke");
						if (html1.length < 1)
							HelperClass.porticoOutput(0,
									"transformed file length=0");
						else {
							if( null == ccs){
								CharacterConversionServiceFactory ccsf = CharacterConversionUtil
									.getFactory();
								ccs = ccsf.createService();
							}
							byte[] html2 = ccs.convert(html1);
							javax.servlet.ServletOutputStream servletoutputstream = httpservletresponse
									.getOutputStream();
							servletoutputstream.write(html2);
							servletoutputstream.flush();
						}
					} else
						HelperClass.porticoOutput(0,
								"fetchedLeadMDFile length=0");
				}
			} else
				HelperClass.porticoOutput(0, "cuId=" + cuId
						+ " is not renderable!");
		} catch (Exception exception) {
			exception.printStackTrace();
			if (Trace.GETCONTENT)
				log("GetContentServlet: Exception " + exception.toString());
			throw new ServletException(exception);
		} finally {
			HttpSessionState
					.releaseHttpSession(httpservletrequest.getSession());
			try {
				if(null != at)
				{
			    	at.flush();
			    	at.clearSessionContext();
			    }
			} catch (Exception exception) {
			}
		}
	}

	private static final String PARAM_OBJECTID = "objectId";

	private static final String PARAM_FORMAT = "format";

	private static IDfSession m_dfSession;
}
