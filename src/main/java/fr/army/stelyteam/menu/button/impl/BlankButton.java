package fr.army.stelyteam.menu.button.impl;

import org.bukkit.event.inventory.InventoryClickEvent;

import fr.army.stelyteam.menu.button.Button;
import fr.army.stelyteam.menu.button.container.ButtonContainer;
import fr.army.stelyteam.menu.button.container.EmptyContainerContent;
import fr.army.stelyteam.menu.button.template.ButtonTemplate;

public class BlankButton extends Button<ButtonContainer<EmptyContainerContent>> {

    public BlankButton(ButtonTemplate buttonTemplate, ButtonContainer<EmptyContainerContent> buttonContainer) {
        super(buttonTemplate, buttonContainer);
    }

    @Override
    public void onClick(InventoryClickEvent clickEvent) {
    }
    
}
