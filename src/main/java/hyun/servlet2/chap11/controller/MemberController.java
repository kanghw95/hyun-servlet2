package hyun.servlet2.chap11.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import hyun.servlet2.chap11.dao.DaoException;
import hyun.servlet2.chap11.dao.Member;
import hyun.servlet2.chap11.dao.MemberDao;
import hyun.servlet2.chap11.dao.MemberDaoImpl;
import hyun.servlet2.web.common.Controller;
import hyun.servlet2.web.common.RequestMapping;
import hyun.servlet2.web.common.RequestMapping.RequestMethod;

@Controller
public class MemberController {

	Logger logger = LogManager.getLogger();

	MemberDao memberDao = null;

	public MemberController() throws Exception {
		memberDao = new MemberDaoImpl();
	}

	@RequestMapping(value = "/register/step2", method = RequestMethod.POST)
	public void step2(HttpServletRequest request,
			HttpServletResponse response) {
		String agree = request.getParameter("agree");

		if (!"true".equals(agree)) {// 동의하지 않으면 step1으로 돌아감
			logger.debug("약관에 동의하지 않았습니다.");
			forward("/WEB-INF/jsp/register/step1.jsp", request, response);
		} else { // 동의하면 step2로 forward
			forward("/WEB-INF/jsp/register/step2.jsp", request, response);
		}
	}

	@RequestMapping(value = "/register/step3", method = RequestMethod.POST)
	public void step3(HttpServletRequest request,
			HttpServletResponse response) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String name = request.getParameter("name");

		// 회원정보 저장
		Member member = new Member(email, password, name);
		try {
			memberDao.insert(member);
			logger.debug("회원 정보를 저장했습니다. {}", member);
			forward("/WEB-INF/jsp/register/step3.jsp", request, response);
		} catch (DaoException e) {
			logger.error(e.getMessage());
			forward("/WEB-INF/jsp/register/step2.jsp", request, response);
		}
	}

	@RequestMapping(value = "/members", method = RequestMethod.GET)
	public void members(HttpServletRequest request,
			HttpServletResponse response) {

		String pageStr = request.getParameter("page");
		int page = 1;
		if (pageStr != null) {
			try { // 가져올 페이지 수
				page = Integer.parseInt(request.getParameter("page"));
			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
			}
		}

		// 페이지 당 가져오는 행의 수
		final int COUNT = 100;
		// 시작점
		int offset = (page - 1) * COUNT;

		List<Member> memberList = memberDao.selectAll(offset, COUNT);

		int totalCount = memberDao.countAll();

		request.setAttribute("totalCount", totalCount);
		request.setAttribute("members", memberList);
		forward("/WEB-INF/jsp/members.jsp", request, response);
	}

	/**
	 * request forward
	 */
	private void forward(String url, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			request.getRequestDispatcher(url).forward(request, response);
		} catch (ServletException | IOException e) {
			logger.error(e.getMessage());
		}
	}
}