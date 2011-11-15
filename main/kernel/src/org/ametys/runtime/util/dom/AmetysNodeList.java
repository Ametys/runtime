package org.ametys.runtime.util.dom;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple implementation of {@link NodeList} backed by a {@link List}.
 */
public class AmetysNodeList implements NodeList
{
    private List<? extends Node> _list;
    
    /**
     * Constructor.
     * @param list the wrapped list.
     */
    public AmetysNodeList(List<? extends Node> list)
    {
        _list = list;
    }
    
    @Override
    public int getLength()
    {
        return _list.size();
    }
    
    @Override
    public Node item(int index)
    {
        if (index < 0 || index > getLength())
        {
            return null;
        }
        
        return _list.get(index);
    }
}
