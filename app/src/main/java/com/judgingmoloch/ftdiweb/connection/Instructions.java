package com.judgingmoloch.ftdiweb.connection;

public class Instructions {
    public String body;
    public String name;
    public String description;
    public int pk;

    public Instructions(String name, String description, String body, int pk) {
        this.name = name;
        this.description = description;
        this.body = body;
        this.pk = pk;
    }

    public class InstructionOverview {
        public int pk;
        public String name;
        public String description;

        public InstructionOverview(String name, String description, int pk) {
            this.name = name;
            this.description = description;
            this.pk = pk;
        }
    }

    public class InstructionList {
        public InstructionOverview[] objects;

        public InstructionList(InstructionOverview[] objects) {
            this.objects = objects;
        }
    }
}
