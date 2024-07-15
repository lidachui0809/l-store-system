package com.ldc.store.modules.file.event;

import com.ldc.store.modules.file.context.DeleteFileContext;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeleteFileEvent extends ApplicationEvent {

    private DeleteFileContext context;

    public DeleteFileEvent(Object source, DeleteFileContext context) {
        super(source);
        this.context = context;
    }


}
