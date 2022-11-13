package fileprocessing3;

import java.util.Scanner;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class AVLtree {
	public static void main(String[] args) throws IOException {
		AVLtree1 a = new AVLtree1();
		Scanner in = new Scanner(new File("AVL-input.txt"));
		while(in.hasNext()) {
			char c = in.next().charAt(0);
			int i = in.nextInt();
			if(c == 'i') {
				a.insertAVL(a.root, i);
			} else if(c == 'd') {
				a.deleteAVL(a.root, i);
			}
			a.inorder(a.root);
			System.out.println();
		}
		
	}
}

class TreeNode {
	int key;
	TreeNode left;
	TreeNode right;
	int heightL;
	int heightR;
	
	TreeNode() {
		this.left = null;
		this.right = null;
	}
	
	TreeNode(int key) {
		this.key = key;
		this.left = null;
		this.right = null;
		this.heightL = 0;
		this.heightR = 0;
	}
}

class AVLtree1 {
	TreeNode root = null;
	Stack<TreeNode> stack = new Stack<>();
	
	public int insertBST(TreeNode t, int key) {
		TreeNode p = t;
		TreeNode q = null;
		while(p != null) {
			if(key == p.key) {
				System.out.println("i " + key + " : " + "The key already exists"); //이미 key값이 있으면 메시지 출력  0을 return
				return 0;
			} else if(key < p.key) {
				q = p;
				stack.push(p);
				p = p.left;
			} else {
				q = p;
				stack.push(p);
				p = p.right;
			}
		}
		TreeNode newNode = new TreeNode(key);
		if(t == null) {
			t = newNode;
			root = newNode;
		} else if(key < q.key) {
			q.left = newNode;
		} else {
			q.right = newNode;
		}
		return 1; //성공적으로 insert했으면 1을 리턴
	}
	
	public void insertAVL(TreeNode t, int key) {
		
		if(insertBST(t, key) == 0) { // insert에 실패했으면 바로 리턴 
			return ;
		}
		
		List<Object> checkResult = checkBalance2(key);
		String rotationType = (String)checkResult.get(0);
		TreeNode newP = (TreeNode)checkResult.get(1); //문제가 발생한 노드 
		TreeNode newQ = (TreeNode)checkResult.get(2); // 문제가 발생한 노드의 부모 노드 
		rotateTree(t, rotationType, newP, newQ);
		System.out.print(rotationType + " ");
		stack.clear(); // stack 초기화
	}
	
	public int deleteBST(TreeNode t, int key) {
		TreeNode p = t;
		TreeNode q = null;
		while(key != p.key) {
			if (key < p.key) {
				stack.push(p);
				q = p;
				p = p.left;
			} else {
				stack.push(p);
				q = p;
				p = p.right;
			}
			if(p == null) {
				System.out.println("d " + key + " : The key does not exist"); //없는 키 값이면 0을 return
				stack.clear();
				return 0;
			}
		}
		if(p.left != null && p.right != null) { //차수가 2일 경우 서브트리의 높이와 하위노드의 갯수에 따라 leftMax or rightMin key 를 탐색 
			TreeNode r;
			stack.push(p);
			if(p.heightL > p.heightR) {
				r = maxNode(p.left);
				p.key = r.key;
				q = p;
				p = p.left;
			} else if(p.heightL < p.heightR) {
				r = minNode(p.right);
				p.key = r.key;
				q = p;
				p = p.right;
			} else if(noNodes(p.left) >= noNodes(p.right)) {
				r = maxNode(p.left);
				p.key = r.key;
				q = p;
				p = p.left;
			} else {
				r = minNode(p.right);
				p.key = r.key;
				q = p;
				p = p.right;
			}
			while(r.key != p.key) { // 찾은 key값까지 경로 탐색 
				if(r.key < p.key) {
					stack.push(p);
					q = p;
					p = p.left;
				} else if(r.key > p.key) {
					stack.push(p);
					q = p;
					p = p.right;
				}
			}
		}
		if(p.left != null) { // 차수가 1인경우 중 왼쪽에 노드가 있을때 
			if(q == null) {
				root = p.left;
			} else if(p.key < q.key) {
				q.left = p.left;
			} else if(p.key > q.key) {
				q.right = p.left;
			}
		} else if(p.right != null) { // 차수가 1인경우 중 오른쪽에 노드가 있을때 
			if(q == null) {
				root = p.right;
			} else if(p.key < q.key) {
				q.left = p.right;
			} else if(p.key > q.key) {
				q.right = p.right;
			}
		}
		
		if(p.left == null && p.right == null) { // 차수가 0일때 
			if(q == null) {
				root = null;
			} else if(p.key <= q.key) {
				q.left = null;
			} else if(p.key >= q.key) {
				q.right = null;
			}
		}
		return 1;
	}
	
	public void deleteAVL(TreeNode t, int key) {
		if(deleteBST(t, key) == 0) { // 삭제에 실패했을 경우 바로 리턴 
			return ;
		}
		
		List<Object> checkResult = checkBalance2(key);
		String rotationType = (String)checkResult.get(0);
		TreeNode newP = (TreeNode)checkResult.get(1);
		TreeNode newQ = (TreeNode)checkResult.get(2);
		rotateTree(t, rotationType, newP, newQ);
		System.out.print(rotationType + " ");
		stack.clear();
	}
	
	public List<Object> checkBalance2(int key) { // 스택에 있는 노드들을 하나씩 꺼내서 balance factor를 다시 계산 
		String rotationType = "NO";
		while(stack.empty()==false) {
			TreeNode p = stack.peek();
			TreeNode q = null;
			if(p.left == null) {
				p.heightL = 0;
			} else {
				p.heightL = Math.max(p.left.heightL, p.left.heightR) + 1;				
			}
			if(p.right == null) {
				p.heightR = 0;
			} else {
				p.heightR = Math.max(p.right.heightL, p.right.heightR) + 1;				
			}
			stack.pop();
			if(p.heightL - p.heightR < -1 || p.heightL - p.heightR > 1) { // balance factor에 문제가 생겼을 경우 rotation type을 결정 
				if(stack.empty()) {
					q = null;
				} else {
					q = stack.peek();
					stack.pop();					
				}
				if(p.heightL > p.heightR) {
					if(p.left.heightL > p.left.heightR) {
						rotationType = "LL";
						return Arrays.asList(rotationType, p, q);
					} else {
						rotationType = "LR";
						return Arrays.asList(rotationType, p, q);
					}
				} else {
					if(p.right.heightL > p.right.heightR) {
						rotationType = "RL";
						return Arrays.asList(rotationType, p, q);
					} else {
						rotationType = "RR";
						return Arrays.asList(rotationType, p, q);
					}
				}
			}
			
		}
		return Arrays.asList(rotationType, null, null);
		
	}
	
	public void rotateTree(TreeNode t, String rotationType, TreeNode newP, TreeNode newQ) { //인자로 rotation type을 전달받아 처리 
		switch(rotationType) {
		case "LL": // 노드의 회전 및 balance factor 재계산 
			if(newQ == null) { 
				root = newP.left;
				TreeNode temp = newP.left.right;
				newP.left.right = newP;
				newP.left = temp;
				newP.heightL = root.heightR;
				root.heightR = Math.max(newP.heightL, newP.heightR) + 1;
				break;
			}
			if(newP.key < newQ.key) {
				newQ.left = newP.left;
				TreeNode temp = newP.left.right;
				newP.left.right = newP;
				newP.left = temp;
				newP.heightL = newQ.left.heightR;
				newQ.left.heightR = Math.max(newP.heightL, newP.heightR) + 1;
			} else {
				newQ.right = newP.left;
				TreeNode temp = newP.left.right;
				newP.left.right = newP;
				newP.left = temp;
				newP.heightL = newQ.right.heightR;
				newQ.right.heightR = Math.max(newP.heightL, newP.heightR) + 1;
			}
			/////
			while(stack.empty() == false) { // root부터 해당 노드사이의 경로상에 있는 노드들의 balance factor를 재계산 
				TreeNode p = stack.peek();
				if(p.left == null) {
					p.heightL = 0;
				} else {
					p.heightL = Math.max(p.left.heightL, p.left.heightR) + 1;				
				}
				if(p.right == null) {
					p.heightR = 0;
				} else {
					p.heightR = Math.max(p.right.heightL, p.right.heightR) + 1;				
				}
				stack.pop();
				
			}
			/////
			break;
		case "RR":
			if(newQ == null) {
				root = newP.right;
				TreeNode temp1 = newP.right.left;
				newP.right.left = newP;
				newP.right = temp1; 
				newP.heightR = root.heightL;
				root.heightL = Math.max(newP.heightL, newP.heightR) + 1;
				break;
			}
			if(newP.key > newQ.key) {
				newQ.right = newP.right;
				TreeNode temp1 = newP.right.left;
				newP.right.left = newP;
				newP.right = temp1; 
				newP.heightR = newQ.right.heightL;
				newQ.right.heightL = Math.max(newP.heightL, newP.heightR) + 1;
			} else {
				newQ.left = newP.right;
				TreeNode temp1 = newP.right.left;
				newP.right.left = newP;
				newP.right = temp1;
				newP.heightR = newQ.left.heightL;
				newQ.left.heightL = Math.max(newP.heightL, newP.heightR) + 1;
			}
			/////
			while(stack.empty() == false) {
				TreeNode p = stack.peek();
				if(p.left == null) {
					p.heightL = 0;
				} else {
					p.heightL = Math.max(p.left.heightL, p.left.heightR) + 1;				
				}
				if(p.right == null) {
					p.heightR = 0;
				} else {
					p.heightR = Math.max(p.right.heightL, p.right.heightR) + 1;				
				}
				stack.pop();
				
			}
			/////
			break;
		case "LR":
			if(newQ == null) {
				root = newP.left.right;
				TreeNode temp21 = newP.left.right.left;
				TreeNode temp22 = newP.left.right.right;
				newP.left.right.left = newP.left;
				newP.left.right.right = newP;
				newP.left.right = temp21;
				newP.left = temp22;
				if(temp21 == null) {
					root.left.heightR = 0;
				} else {
					root.left.heightR = Math.max(temp21.heightL, temp21.heightR) + 1;
				}
				root.heightL = Math.max(root.left.heightL, root.left.heightR) + 1;
				if(newP.left == null) {
					newP.heightL = 0;
				} else {
					newP.heightL = Math.max(newP.left.heightL, newP.left.heightR) + 1;					
				}
				root.heightR = Math.max(newP.heightL, newP.heightR) + 1;
				break;
			}
			if(newP.key < newQ.key) {
				newQ.left = newP.left.right;
				TreeNode temp21 = newP.left.right.left;
				TreeNode temp22 = newP.left.right.right;
				newP.left.right.left = newP.left;
				newP.left.right.right = newP;
				newP.left.right = temp21;
				newP.left = temp22; 
			if(temp21 == null) {
				newQ.left.left.heightR = 0;
			} else {
				newQ.left.left.heightR = Math.max(temp21.heightL, temp21.heightR) + 1;
			}
			newQ.left.heightL = Math.max(newQ.left.left.heightL, newQ.left.left.heightR) + 1;
			if(newP.left == null) {
				newP.heightL = 0;
			} else {
				newP.heightL = Math.max(newP.left.heightL, newP.left.heightR) + 1;
			}
			newQ.left.heightR = Math.max(newP.heightL, newP.heightR) + 1;
			} else {
				newQ.right = newP.left.right;
				TreeNode temp21 = newP.left.right.left;
				TreeNode temp22 = newP.left.right.right;
				newP.left.right.left = newP.left;
				newP.left.right.right = newP;
				newP.left.right = temp21;
				newP.left = temp22;
				if(temp21 == null) {
					newQ.right.left.heightR = 0;
				} else {
					newQ.right.left.heightR = Math.max(temp21.heightL, temp21.heightR) + 1;
				}
				newQ.right.heightL = Math.max(newQ.right.left.heightL, newQ.right.left.heightR) + 1;
				if(newP.left == null) {
					newP.heightL = 0;
				} else {
					newP.heightL = Math.max(newP.left.heightL, newP.left.heightR) + 1;
				}
				newQ.right.heightR = Math.max(newP.heightL, newP.heightR) + 1;
			}
			/////
			while(stack.empty() == false) {
				TreeNode p = stack.peek();
				if(p.left == null) {
					p.heightL = 0;
				} else {
					p.heightL = Math.max(p.left.heightL, p.left.heightR) + 1;				
				}
				if(p.right == null) {
					p.heightR = 0;
				} else {
					p.heightR = Math.max(p.right.heightL, p.right.heightR) + 1;				
				}
				stack.pop();
				
			}
			/////
			break;
		case "RL":
			if(newQ == null) {
				root = newP.right.left;
				TreeNode temp31 = newP.right.left.right;
				TreeNode temp32 = newP.right.left.left;
				newP.right.left.right = newP.right;
				newP.right.left.left = newP;
				newP.right.left = temp31;
				newP.right = temp32;
				if(temp31 == null) {
					root.right.heightL = 0;
				} else {
					root.right.heightL = Math.max(temp31.heightL, temp31.heightR) + 1;
				}
				root.heightR = Math.max(root.right.heightR, root.right.heightL) + 1;
				if(newP.right == null) {
					newP.heightR = 0;
				} else {
					newP.heightR = Math.max(newP.right.heightR, newP.right.heightL) + 1;					
				}
				root.heightL = Math.max(newP.heightR, newP.heightL) + 1;
				break;
			}
			if(newP.key > newQ.key) {
				newQ.right = newP.right.left;
				TreeNode temp31 = newP.right.left.right;
				TreeNode temp32 = newP.right.left.left;
				newP.right.left.right = newP.right;
				newP.right.left.left = newP;
				newP.right.left = temp31;
				newP.right = temp32;
			if(temp31 == null) {
				newQ.right.right.heightL = 0;
			} else {
				newQ.right.right.heightL = Math.max(temp31.heightL, temp31.heightR) + 1;
			}
			newQ.right.heightR = Math.max(newQ.right.right.heightR, newQ.right.right.heightL) + 1;
			if(newP.right == null) {
				newP.heightR = 0;
			} else {
				newP.heightR = Math.max(newP.right.heightR, newP.right.heightL) + 1;					
			}
			newQ.right.heightL = Math.max(newP.heightR, newP.heightL) + 1;
			} else {
				newQ.left = newP.right.left;
				TreeNode temp31 = newP.right.left.right;
				TreeNode temp32 = newP.right.left.left;
				newP.right.left.right = newP.right;
				newP.right.left.left = newP;
				newP.right.left = temp31;
				newP.right = temp32;
				if(temp31 == null) {
					newQ.left.right.heightL = 0;
				} else {
					newQ.left.right.heightL = Math.max(temp31.heightL, temp31.heightR) + 1;
				}
				newQ.left.heightR = Math.max(newQ.left.right.heightR, newQ.left.right.heightL) + 1;
				if(newP.right == null) {
					newP.heightR = 0;
				} else {
					newP.heightR = Math.max(newP.right.heightR, newP.right.heightL) + 1;					
				}
				newQ.left.heightL = Math.max(newP.heightR, newP.heightL) + 1;
			}
			/////
			while(stack.empty() == false) {
				TreeNode p = stack.peek();
				if(p.left == null) {
					p.heightL = 0;
				} else {
					p.heightL = Math.max(p.left.heightL, p.left.heightR) + 1;				
				}
				if(p.right == null) {
					p.heightR = 0;
				} else {
					p.heightR = Math.max(p.right.heightL, p.right.heightR) + 1;				
				}
				stack.pop();
				
			}
			/////
			break;
		}
	}
	
	public void inorder(TreeNode t) {
		if(t != null) {
			inorder(t.left);
			//System.out.print();
			System.out.print("(" + t.key + ", " + (t.heightL-t.heightR) + ")" + " ");
			//System.out.print(t.key + " ");
			inorder(t.right);
		}
	}
	
	public TreeNode maxNode(TreeNode t) {
		TreeNode p = t;
		while(p.right != null) {
			p = p.right;
		}
		return p;
	}
	
	public TreeNode minNode(TreeNode t) {
		TreeNode p = t;
		while(p.left != null) {
			p = p.left;
		}
		return p;
	}
	
	public int noNodes(TreeNode t) {
		TreeNode p = root;
		while(p.key != t.key) {
			if(t.key < p.key) {
				p = p.left;
			} else {
				p = p.right;
			}
		}
		int sum = noNodessub(p);
		return sum - 1;
	}
	
	public int noNodessub(TreeNode t) {
		int sum;
		if(t != null) {
			sum = noNodessub(t.left) + noNodessub(t.right) + 1;
		} else {
			sum = 0;
		}
		return sum;
	}
}

