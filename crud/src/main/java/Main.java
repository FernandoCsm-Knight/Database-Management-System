import crud.indexes.Trees.BPlusTree;
import logic.SystemSpecification;

public class Main implements SystemSpecification {
    
    public static void main(String[] args) throws Exception {
        BPlusTree tree = new BPlusTree(3, "BPTree.db");
        
        tree.reset();
        tree.insert(1, 10);
        tree.insert(2, 10);
        tree.insert(3, 10);
        tree.insert(4, 10);
        tree.insert(5, 10);
        tree.insert(6, 10);
        tree.insert(7, 10);

        System.out.println(tree.delete(4));
        System.out.println(tree.delete(3));
        System.out.println(tree.delete(6));
        System.out.println(tree.delete(7));
        System.out.println(tree.delete(2));
        System.out.println(tree.delete(5));
        System.out.println(tree.delete(1));

        tree.toJsonFile();
    }   

}
