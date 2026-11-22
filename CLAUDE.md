# CLAUDE.md — Football Graph Simulation Engine

This file provides working instructions for Claude Code when developing this repository. Treat the repository's existing code as the source of truth; inspect it before proposing or making structural changes.

## 1. Project Overview

Build a football (soccer) simulation engine in Java that represents formations, player positions, and tactical relationships as a dynamic graph over a two-dimensional pitch.

The long-term goal is to progress from a well-designed domain model to a tactical and match simulation in which players make decisions, move, interact, and execute actions according to their attributes, roles, team instructions, and the current match state.

Core concepts:

- **Pitch:** A bounded 2D playing surface, measured in metres, with a defined length and width.
- **Players:** Entities with positions, roles, attributes, and match-specific state.
- **Formations:** Arrangements of player-position nodes on the pitch. A formation describes a starting structure, not a rigid constraint on movement.
- **Graph:** Nodes represent players or player-position states; edges represent possible or active tactical relationships and actions.
- **Tactical edges:** Typed and potentially weighted connections such as passes, through balls, switches, overlaps, pressing relationships, marking, and defensive coverage.
- **Simulation:** A time-stepped or event-driven system that updates the ball, players, team shape, and match state.

The graph should be useful for reasoning about football, not merely a visual representation of player locations. Design it so that graph structure and edge weights can evolve as the match situation changes.

## 2. Working Principles

1. **Understand before changing.** Inspect the project structure, existing classes, dependencies, and tests before editing. Preserve established conventions unless there is a clear reason to change them.
2. **Build incrementally.** Prefer small, compiling, testable steps over implementing the entire simulation at once.
3. **Keep the domain model coherent.** Separate football concepts from rendering, input/output, and simulation orchestration.
4. **Use object-oriented design deliberately.** Apply encapsulation, composition, interfaces, and polymorphism where they make the model clearer. Avoid inheritance or design patterns added only for their own sake.
5. **Model real concepts explicitly.** Use meaningful types for players, positions, actions, edges, formations, and match state rather than representing everything as primitive values or loosely structured strings.
6. **Make behaviour deterministic when practical.** Randomness used for simulation should be controllable through an injected or configured random source so tests can be repeatable.
7. **Avoid premature complexity.** Do not introduce databases, networking, machine learning, a UI framework, or external graph libraries unless the task requires them and the existing project supports the choice.
8. **Explain important trade-offs.** When a design choice affects extensibility, performance, or football realism, briefly state the reasoning.

## 3. Repository and Architecture Guidance

First inspect the actual repository. Do not assume that suggested packages or classes below already exist. Adapt to the current structure rather than creating duplicate abstractions.

A reasonable separation of responsibilities is:

- `model`: Core domain objects such as `Pitch`, `Player`, `Position`, `Team`, `Formation`, `Ball`, and match state.
- `graph`: Graph representation, nodes, edges, edge types, graph operations, and path or connectivity queries.
- `simulation`: Match loop, tick/event processing, state transitions, and simulation configuration.
- `tactics`: Team instructions, player roles, tactical rules, decision-making, and strategy evaluation.
- `engine` or `service`: Coordination between the model, graph, and simulation where needed.
- `util`: Small, genuinely reusable utilities only.
- `test`: Unit and integration tests, following the repository's existing test layout.

Keep package names consistent with the existing code. For example, the project has used `main.model` for model classes; do not rename packages without a concrete need.

### Dependency direction

Prefer dependencies that point toward the domain model:

- Domain objects should not depend on UI or presentation code.
- Graph structures should operate on well-defined domain concepts without taking responsibility for match rules.
- Tactical decision logic should consume match and graph state, then produce decisions or actions.
- The simulation engine should coordinate updates rather than contain every football rule itself.

Avoid circular dependencies. If two components need to communicate, introduce a small interface or move shared concepts to an appropriate lower-level package.

## 4. Domain Modelling Rules

### Pitch and coordinates

- Use a consistent coordinate system across the entire project. Document the origin and the direction of attack.
- Treat pitch length and width as dimensions, not interchangeable values.
- Validate that dimensions are positive and that positions intended to be on the pitch are within its bounds.
- Keep coordinate units explicit in documentation and naming where ambiguity is possible. The project currently models pitch dimensions in metres.
- Consider whether a position is a geometric location, a player's current location, or a tactical/nominal position. These are related but distinct concepts.

### Players and attributes

- Separate relatively stable player attributes (for example, passing, vision, pace, tackling, positioning, stamina) from dynamic match state (for example, current coordinates, fatigue, possession involvement, and temporary status).
- Avoid inventing an elaborate rating system before the simulation needs it. Start with the attributes required by the feature being implemented.
- Keep ratings within documented bounds and validate input.
- Do not assume every player performs equally well in every role. Role suitability can be introduced as a separate concern.

### Formations and roles

- A formation should define an initial or reference arrangement of player slots/positions.
- Distinguish a formation slot (such as a nominal left-back position) from the player assigned to it and from that player's live coordinates.
- Support different formations without hardcoding one formation into the simulation engine.
- Do not freeze players at formation coordinates during play. Movement and tactical shape changes are central to the simulation.

### Graph model

- Make node and edge semantics explicit. Document what a node represents and what an edge means.
- Use an edge type or equivalent typed representation for different relationships/actions; do not rely on magic strings.
- Store edge weights with a documented meaning and scale. A higher weight might mean greater feasibility, preference, threat, or strength—but do not mix these interpretations.
- Distinguish a possible action from an action that has actually occurred. For example, a potential pass edge is not the same as a completed pass event.
- Consider directionality: passes and many tactical actions are directional, while some relationships may be symmetric.
- Ensure graph updates do not leave stale edges after players move, become unavailable, or the match state changes.
- Avoid duplicating player state inside graph nodes unless there is a clear ownership and synchronisation strategy. Prefer references or stable identifiers where appropriate.
- Choose adjacency lists or another representation based on expected graph operations; do not optimise without evidence.

## 5. Simulation Design

Implement simulation behaviour in stages. A sensible progression is:

1. Create and validate the pitch, teams, players, and formations.
2. Place players and the ball in a valid initial state.
3. Represent players and tactical relationships in the graph.
4. Generate candidate actions such as passes, carries, movement, and defensive pressure.
5. Evaluate candidate actions using relevant attributes, spatial conditions, and tactical instructions.
6. Resolve an action and update the authoritative match state.
7. Recompute or incrementally update affected graph relationships.
8. Add possession, transitions, shots, goals, fouls, restarts, and match timing as separate features.

Do not attempt to implement all football rules in one class. Keep action selection, action evaluation, action resolution, and state updates conceptually distinct.

### Match-state integrity

- Keep one authoritative source of truth for player locations, ball location/possession, score, and match time.
- Validate state transitions. For example, a pass should have a valid origin, target, and ball state.
- Define how simultaneous or competing actions are resolved before introducing parallel simulation.
- Use explicit units for time and distance.
- Avoid hidden state changes inside getters or methods whose names imply read-only behaviour.

## 6. Java Coding Standards

- Follow the Java version and build system already configured in the repository. Do not upgrade them without asking or explaining why it is necessary.
- Use clear class, method, and variable names. Use `PascalCase` for classes and `camelCase` for methods and fields.
- Keep fields `private` by default. Expose behaviour through methods rather than allowing unrestricted mutation.
- Make objects immutable where practical, especially for value objects such as coordinates, positions, and identifiers.
- Use enums for finite, stable sets of domain values such as edge types or player roles.
- Prefer small methods with one clear responsibility.
- Avoid large constructors with many primitive parameters; use value objects or builders only when they improve clarity.
- Validate constructor and method inputs at the boundary where invalid values enter the model.
- Use `List`, `Map`, `Set`, and interfaces where appropriate; avoid exposing mutable internal collections directly.
- Do not use `null` as an ordinary representation of absence when `Optional`, an explicit state, or a suitable type makes the intent clearer. Do not overuse `Optional` for fields or parameters.
- Avoid magic numbers. Name and document values that represent rules, thresholds, or physical constants.
- Add comments to explain intent, assumptions, and non-obvious decisions—not to narrate self-explanatory code.
- Do not add dependencies for functionality that is straightforward to implement with the standard library.

## 7. Testing Expectations

- Add or update tests for every meaningful behaviour change.
- Prefer unit tests for domain rules and graph operations, with integration tests for interactions between the graph and simulation engine.
- Test boundary conditions: pitch edges, invalid dimensions, missing players, empty graphs, disconnected nodes, and invalid actions.
- Test graph directionality, edge types, weight constraints, and edge updates after state changes.
- Test that formations can be instantiated independently and that players can move away from their nominal positions.
- Use deterministic inputs for simulation tests. If randomness is involved, seed or inject it.
- Do not weaken or delete tests simply to make a change pass. Explain any test whose expected behaviour must legitimately change.
- Run the repository's existing test and build commands after changes. If commands cannot be identified, inspect the build files and report what was or was not run.

## 8. Performance and Realism

- Start with correctness and a clear model; optimise only after identifying a real bottleneck.
- Avoid recalculating every possible relationship on every simulation step if profiling later shows this is expensive. Incremental graph updates may then be appropriate.
- Keep football realism configurable where possible. Avoid embedding tactical assumptions as universal rules.
- Treat distances, angles, pressure, passing lanes, player attributes, and tactical instructions as possible inputs to action evaluation, but introduce them only as needed.
- Do not claim the simulation is realistic or statistically validated without testing it against an appropriate reference or dataset.

## 9. Claude Code Workflow

For each task:

1. Restate the requested outcome briefly and inspect the relevant files.
2. Identify the smallest set of classes and tests that should change.
3. Explain the proposed approach before making broad or breaking architectural changes.
4. Implement the feature in small steps, preserving existing public APIs unless a change is required.
5. Run relevant tests and the build.
6. Review the diff for accidental changes, duplicated logic, invalid assumptions, and unfinished code.
7. Summarise what changed, key design decisions, tests run, and any known limitations.

### Before adding a new class

Check whether an existing class already owns the responsibility. Avoid duplicate concepts with slightly different names (for example, `Coordinate`, `Location`, and `Position`) unless they have clearly different meanings.

### Before changing an API

Search for all call sites and tests. Prefer backward-compatible changes where practical. If a breaking change is necessary, update all affected code and explain the migration.

### When requirements are unclear

Make conservative assumptions for reversible implementation details and state them. Ask a focused question before making an irreversible architectural choice or inventing a major game mechanic.

## 10. Out of Scope Unless Requested

Do not independently add:

- A graphical match viewer or animation system. (A team-builder UI is in scope; see section 12.)
- Online multiplayer, networking, or a server API.
- Persistent storage or a database.
- Scraping or external APIs. (Loading real players from locally supplied datasets is in scope; see section 12.)
- Machine-learning-based tactics or player behaviour.
- Full official football-law enforcement.
- A complex physics engine.

These may become useful later, but should not distract from building a sound graph-based simulation core.

## 11. Definition of Done

A change is complete when:

- It meets the requested behaviour and fits the existing architecture.
- Domain invariants are validated and invalid states are handled deliberately.
- Relevant tests are added or updated and pass.
- The project builds successfully, or any build limitation is clearly reported.
- No unrelated files or dependencies were changed.
- The final explanation states what was implemented and what remains incomplete.

## 12. Future Design Notes (Not Yet Implemented)

These are the author's design intentions for later stages. Do not implement them unprompted; use them to keep today's model compatible with them (see section 10 for scope rules).

### Long-term vision

Two teams play a match that can be watched in real time, with a configurable match length and halves. A manager can change team settings while the match runs: formation, mentality, width, and line height. A viewer and live manager controls are a late-stage goal, not a near-term task.

### Player decision-making is the core engine

Each player will eventually have its own decision-maker. It takes the candidate actions (for example, the pass options from the graph), scores every one with a scoring function, and picks the best. Scoring should combine spatial and attribute inputs with team instructions, so the instructions below should be inputs to scoring rather than hardcoded rules.

### Team instructions (set by the manager, separate from the formation)

Team instructions are kept separate from `Formation`. The same formation can run under different instructions.

- **Mentality:** an enum such as `DEFENSIVE`, `BALANCED`, `ATTACKING`. It biases action scores.
  - Defensive, for example protecting a lead: favour short, controlled passes within the defensive area; the ball does not need to reach an attacker.
  - Attacking: favour passes toward attackers over passes to defenders.
- **Compactness / team spread:** how spread out the team is across the pitch. Low-block ("park the bus") play is not a different formation. A 4-4-2 or 5-4-1 stays as it is, but the spread shrinks and players drop back. Without it, a 5-4-1 can still leave the striker alone in the opposition box and the midfield in the middle of the pitch.
- **Width:** how wide the team plays across the pitch.
- **Line height / offset:** how high the defensive line sits. A high value suits an attacking approach, with the defence pushed up.

### Modelling implications

- Instructions modify how formation slot positions are interpreted during play, for example by scaling spread, width, or line height about a reference. They do not change the formation's definition.
- Keep these as explicit typed values with documented ranges, not free-form numbers (section 4). Make them changeable mid-match, so the model must not assume they are fixed at kickoff.
- Whether spread, width and line height are continuous sliders or discrete levels is undecided. Ask before fixing the representation.

### MatchState

A `MatchState` object will be needed as the single authoritative source of truth for the live match (section 5). Decision-makers read it when scoring actions, and action resolution writes to it. Expected contents, to be refined when it is built:

- Every player's current coordinates (and the ball's location and possession).
- Match score and match time (with half).
- Player stamina or fatigue (the author is unsure about this one; confirm before adding it).
- Whatever is needed for rule checks. For example, an offside check on every pass requires the positions of all players, the ball, and each team's attack direction at the moment the pass is made.

Design notes:

- Per section 4, keep stable player attributes separate from dynamic state like stamina and live coordinates. **Decision:** `Player.coordinates` stores the live coordinates. `MatchState` is the single authority for *where state is read from*, not a second store: it reads player coordinates (and other state) from the objects that hold them and relays them to callers. Do not copy coordinates into `MatchState` or the graph.
- Scoring should be able to read `MatchState` without mutating it, for example by exposing a read-only view to decision-makers.
- Match score and time feed the team instructions. A lead late in the match is what would push a manager toward a defensive mentality.

### Tactical shapes (long-term; not part of V1)

Managers reason about named shapes inside a formation, and the model should leave room for them.

- **Lines:** ordered groups of slots, such as the back four, the midfield line, and the attack line (in a 4-3-3: LW, ST, RW). A player breaking their line, for example a full-back pushing up, leaves space behind. That is sometimes right (attacking phases) and sometimes costly (counter-attacks).
- **Units:** other groupings, such as the triangle of two CBs and the holding midfielder in a 4-3-3, or a diamond.

Intended design:

- **Reference layer (`Formation`):** a shape is a named group of slots, stored as data like the passing-options map. It is a template that can be shared across teams and is flipped with the formation.
- **Live layer (graph):** evaluate each shape against live coordinates, for example a line's deviation measured along the attack direction, or a triangle's side lengths versus the reference. The result is a signal, not a hard rule.
- **Decision layer (`tactics`):** shape integrity is one input to action scoring. Its weight depends on context (counter-attack risk, mentality, compactness and line-height instructions). "Don't break the line" is a weighted preference, not a constraint.

Notes:

- Anything that references slots (passing options now, shapes later) must be remapped when `Formation.flipped()` creates new `Slot` objects. Consider stable slot identifiers when shapes are introduced.
- Shape weights are tunable, football-realism assumptions. Keep them configurable and do not claim realism without a reference (section 8).

### Real players and the team-builder UI (in scope, next phase)

The project uses real players (current and historical, e.g. Mbappe, Messi, Yamal, Maldini, Maradona), loaded from datasets the author supplies locally (CSV, and the project's own JSON format). Not random or invented players.

- Parsing and writing live in their own package (e.g. `data` or `io`), not in `model`. Model classes must not know about file formats; the loader maps rows to model objects (section 3 dependency direction).
- JSON reading and writing are custom-built (no JSON library), in keeping with the standard-library-first rule. CSV needs only the standard library.
- Add only the player attributes the current feature needs (section 4), and map dataset columns to them explicitly. Document the scale and meaning of each rating, and validate it on load.
- Check each dataset's licence and source before committing its data to the repository.
- The first UI is a team builder: choose a formation, load a squad, and see the layout on a pitch. It is not a match viewer. The toolkit is undecided (Swing vs JavaFX); the UI must depend on the model, never the reverse.
