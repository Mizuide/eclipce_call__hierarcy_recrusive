package hello.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		String projectName = "sample";
		String fqcn = "pkg.ClassA";
		String methodName = "methodA";

		IType type;
		IMember[] members;
		try {
			type = findType(projectName, fqcn);
			members = findMember(type, methodName);
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new ExecutionException("");
		}

		try {
			printResult(members);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
//		for (IMember im : members) {
//			HashSet<IMethod> result = CallerUtil.getCallers(im);
//
//			for (IMethod r : result) {
//				//クラス名
//				sb.append(r.getParent().getElementName());
//				//メソッド名
//				sb.append(r.getElementName());
//				//itype
//				r.getDeclaringType();
//
//			}
//		}

		System.out.println("end");
//		MessageDialog.openInformation(
//				window.getShell(),
//				"method",
//				sb.toString());
		return null;
	}

	private void printResult(IMember[] members) throws JavaModelException {
		StringBuilder sb = new StringBuilder();
		for (IMember im : members) {
			HashSet<IMember> result = CallerUtil.getCallers(im);

			for (IMember r : result) {
				//クラス名
				sb.append(r.getParent().getElementName());
				//メソッド名
				sb.append(r.getElementName());
				//itype
				printResult(findMember(r.getDeclaringType(),r.getElementName()));

			}
			System.out.println(sb);
		}
	}

	private IMethod[] findMethod(IType type, String methodName) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		List<IMethod> result = new ArrayList<>();

		for (IMethod method : methods) {
			if (method.getElementName().equals(methodName)) {
				result.add(method);
			}
		}

		return result.toArray(new IMethod[result.size()]);
	}

	private IMember[] findMember(IType type, String memberName) throws JavaModelException {

		IMethod[] methods = type.getMethods();
		List<IMember> result = new ArrayList<>();

		for (IMethod method : methods) {
			if (method.getElementName().equals(memberName)) {
				result.add(method);
			}
		}

		IField[] members = type.getFields();

		for (IField member : members) {
			if (member.getElementName().equals(memberName)) {
				result.add(member);
			}
		}

		return result.toArray(new IMember[result.size()]);
	}

	private IType findType(String projectName, String fullName) throws JavaModelException {
		IJavaProject jp = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));

		return jp.findType(fullName);
	}

	static class CallerUtil {

		public static HashSet<IMember> getCallers(IMember member) {
			CallHierarchy callHierarchy = CallHierarchy.getDefault();

			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			callHierarchy.setSearchScope(scope);

			IMember[] members = { member };
			MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
			HashSet<IMember> result = new HashSet<IMember>();

			for (MethodWrapper mw : methodWrappers) {
				MethodWrapper[] calls = mw.getCalls(new NullProgressMonitor());
				result.addAll(getMethods(calls));
			}

			return result;
		}

		private static HashSet<IMember> getMethods(MethodWrapper[] methodWrappers) {
			HashSet<IMember> result = new HashSet<>();

			for (MethodWrapper mw : methodWrappers) {

				IMember method = mw.getMember();

//				if (method != null) {
					result.add(method);
//				}
			}

			return result;
		}

		private static IMethod toMethod(MethodWrapper methodWrappers) {
			IMember member = methodWrappers.getMember();

			if (member.getElementType() == IJavaElement.METHOD) {
				return (IMethod) methodWrappers.getMember();
			} else {
				System.out.println("IGNORE:" + member.toString());
			}

			return null;
		}
	}
	//
	//    class CallTracer {
	//
	//        private IMember target;
	//        private List<CallTracer> callers;
	//
	//        public CallTracer(IMember member) {
	//            target = member;
	//            callers = new ArrayList<CallTracer>();
	//        }
	//
	//        public void addCaller(CallTracer c) {
	//            callers.add(c);
	//        }
	//
	//        public IMember getTarget() {
	//            return target;
	//        }
	//
	//
	//        public void trace() {
	//            trace(0);
	//        }
	//
	//        private void trace(int idt) {
	//            printTrace(idt);
	//
	//            for (CallTracer tracer : callers) {
	//                idt++;
	//                tracer.trace(idt);
	//            }
	//        }
	//
	//        public void traceSimple() {
	//            traceSimple(true, 0);
	//        }
	//
	//        public void traceSimple(boolean pltFlg, int idt) {
	//            if (pltFlg) {
	//                printTrace(idt);
	//                idt++;
	//            }
	//
	//            if (callers.isEmpty()) {
	//                printTrace(idt);
	//            } else {
	//                for (CallTracer tracer : callers) {
	//                    tracer.traceSimple(false, idt);
	//                }
	//            }
	//        }
	//
	//        private void printTrace(int idt) {
	//            System.out.println(getIdt(idt) + " class:" + target.getParent().getElementName());
	//            System.out.println(getIdt(idt) + "method:" + target.getElementName());
	//        }
	//
	//        private String getIdt(int idt) {
	//            StringBuilder sb = new StringBuilder();
	//
	//            for (int i = 0; i < idt; i++) {
	//                sb.append("\t");
	//            }
	//
	//            return sb.toString();
	//        }
	//    }
}
